package gif.block.labeled;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Optional;

import gif.block.labeled.extension.GraphicControlExtension;
import gif.data.ColorTable;
import gif.data.DataBlock;
import gif.data.GlobalColorTable;
import gif.data.Pixel;
import gif.data.State;
import gif.data.Unsigned;
import gif.data.exception.InvalidValue;
import gif.data.exception.OutOfBounds;
import gif.data.exception.ParseException;
import gif.lzw.BitStream;
import gif.lzw.Lzw;

public record Image(
  Unsigned.Short left,
  Unsigned.Short top,
  Unsigned.Short width,
  Unsigned.Short height,
  Optional<ColorTable> colorTable,
  boolean isInterlaced,
  int minimumCodeSize,
  DataBlock data,
  Optional<GraphicControlExtension> graphicControlExtension
) implements LabeledBlock {
  public static final byte label = 0x2c;

  public Image(Unsigned.Short left, Unsigned.Short top, Unsigned.Short width, Unsigned.Short height, Optional<ColorTable> colorTable, boolean isInterlaced, int minimumCodeSize, DataBlock data, Optional<GraphicControlExtension> graphicControlExtension) {
    this.left                    = Objects.requireNonNull(left  );
    this.top                     = Objects.requireNonNull(top   );
    this.width                   = Objects.requireNonNull(width );
    this.height                  = Objects.requireNonNull(height);
    this.colorTable              = colorTable.map(Objects::requireNonNull);
    this.isInterlaced            = isInterlaced;
    this.minimumCodeSize         = OutOfBounds.check("minimum code size", minimumCodeSize, 0, 11);  // minimum size of 11 gives initial size of 12, which is the max
    this.data                    = Objects.requireNonNull(data);
    this.graphicControlExtension = graphicControlExtension.map(Objects::requireNonNull);
  }

  public static Image readFrom(InputStream stream, State state) throws IOException, ParseException {
    var left   = Unsigned.Short.readFrom(stream);
    var top    = Unsigned.Short.readFrom(stream);
    var width  = Unsigned.Short.readFrom(stream);
    var height = Unsigned.Short.readFrom(stream);

    var packedFields = Unsigned.Byte.readFrom(stream).intValue();

    var hasColorTable = ((packedFields >> 7) & 1) == 1;
    var isInterlaced  = ((packedFields >> 6) & 1) == 1;

    var isSorted      = ((packedFields >> 5) & 1) == 1;
    var packedSize    =  (packedFields >> 0) & 7;
    Optional<ColorTable> colorTable = Optional.empty();
    if (hasColorTable) {
      var table = ColorTable.readFrom(stream, packedSize, isSorted);
      state.graphicControlExtension
        .flatMap(GraphicControlExtension::transparentColorIndex)
        .map(Unsigned.Byte::intValue)
        .map(index -> OutOfBounds.check("transparent color index", index, 0, table.colors().size() - 1));

      colorTable = Optional.of(table);
    } else {
      if (isSorted)
        System.err.println("WARNING: missing local color table marked as sorted (not wrong but still)");

      if (packedSize != 0)
        System.err.println("WARNING: missing local color table has nonzero packed size");
    }

    var minimumCodeSize = Unsigned.Byte.readFrom(stream).intValue();
    var data = DataBlock.readFrom(stream);

    var graphicControlExtension = state.graphicControlExtension;

    return new Image(left, top, width, height, colorTable, isInterlaced, minimumCodeSize, data, graphicControlExtension);
  }

  @Override
  public byte label() { return Image.label; }

  public Pixel[][] getPixels(Optional<GlobalColorTable> globalColorTable) throws ParseException {
    var indices = Lzw.decode(new BitStream(data), minimumCodeSize);
    InvalidValue.check("image data pixel count", indices.size(), width.intValue() * height.intValue());

    var colors = this.colorTable
      .or(() -> globalColorTable.map(GlobalColorTable::colorTable))
      .map(ColorTable::colors)
      .orElseThrow(() -> new ParseException("no color table"));

    var transparentColorIndex = graphicControlExtension
      .flatMap(GraphicControlExtension::transparentColorIndex)
      .map(Unsigned.Byte::intValue)
      .orElse(-1);

    var possibleValues = new Pixel[colors.size()];
    for (var i=0; i < colors.size(); ++i)
      possibleValues[i] = new Pixel(colors.get(i), i == transparentColorIndex);

    var indicesLeft = indices.iterator();
    var result = new Pixel[height.intValue()][width.intValue()];
    for (var row : result)
      for (var x=0; x < width.intValue(); ++x)
        row[x] = possibleValues[indicesLeft.next()];

    return result;
  }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    // can't use ifPresent since writeTo throws
    if (graphicControlExtension.isPresent())
      graphicControlExtension.get().writeTo(stream);

    stream.write(Image.label);

    left  .writeTo(stream);
    top   .writeTo(stream);
    width .writeTo(stream);
    height.writeTo(stream);

    var packedFields =    (isInterlaced     ? 1 << 6 : 0)
                     | colorTable.map(table
                       -> (                   1 << 7    )  // has color table
                        | (table.isSorted() ? 1 << 5 : 0)
                        | (table.packedSize()   << 0    )).orElse(0);
    stream.write(packedFields);

    // can't use ifPresent since writeTo throws
    if (colorTable.isPresent())
      colorTable.get().writeTo(stream);

    stream.write(minimumCodeSize);
    data.writeTo(stream);
  }
}
