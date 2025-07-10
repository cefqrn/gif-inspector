package gif.block.labeled;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import gif.block.labeled.extension.GraphicControlExtension;
import gif.data.Color;
import gif.data.DataBlock;
import gif.data.Pixel;
import gif.data.State;
import gif.data.exception.InvalidValue;
import gif.data.exception.ParseException;
import gif.lzw.BitStream;
import gif.lzw.Lzw;
import gif.module.Read;
import gif.module.Write;

public class Image extends LabeledBlock {
  public static final byte label = 0x2c;

  public final int left;
  public final int top;
  public final int width;
  public final int height;
  public final Optional<List<Color>> colorTable;
  public final boolean isInterlaced;
  public final boolean colorTableIsSorted;
  public final int minimumCodeSize;
  public final DataBlock data;
  public final Optional<GraphicControlExtension> graphicControlExtension;

  public Image(InputStream stream, State state) throws IOException, ParseException {
    left   = Read.U16From(stream);
    top    = Read.U16From(stream);
    width  = Read.U16From(stream);
    height = Read.U16From(stream);

    var packedFields = Read.U8From(stream);

    var hasColorTable  =       ((packedFields >> 7) & 1) == 1;
    isInterlaced       =       ((packedFields >> 6) & 1) == 1;
    colorTableIsSorted =       ((packedFields >> 5) & 1) == 1;

    List<Color> table = null;
    if (hasColorTable) {
      var size         = 1 << (((packedFields >> 0) & 7) + 1);

      table = new ArrayList<>(size);
      for (var i=0; i < size; ++i)
        table.add(new Color(stream));

      table = Collections.unmodifiableList(table);
    }
    colorTable = Optional.ofNullable(table);

    minimumCodeSize = Read.U8From(stream);
    data = DataBlock.readFrom(stream);

    graphicControlExtension = state.graphicControlExtension;
  }

  @Override
  public byte getLabel() { return Image.label; }

  public Pixel[][] getPixels(Optional<List<Color>> globalColorTable) throws ParseException {
    var indices = Lzw.decode(new BitStream(data), minimumCodeSize);

    var expectedPixelCount = width * height;
    if (indices.size() != expectedPixelCount)
      throw new InvalidValue("image data pixel count", indices.size(), expectedPixelCount);

    var colors = this.colorTable
      .or(() -> globalColorTable)
      .orElseThrow(() -> new ParseException("no color table"));

    var transparentColorIndex = graphicControlExtension
      .flatMap(e -> e.transparentColorIndex)
      .orElse(-1);

    var possibleValues = new Pixel[colors.size()];
    for (var i=0; i < colors.size(); ++i)
      possibleValues[i] = new Pixel(colors.get(i), i == transparentColorIndex);

    var indicesLeft = indices.iterator();
    var result = new Pixel[height][width];
    for (var row : result)
      for (var x=0; x < width; ++x)
        row[x] = possibleValues[indicesLeft.next()];

    return result;
  }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    // can't use ifPresent since writeTo throws
    if (graphicControlExtension.isPresent())
      graphicControlExtension.get().writeTo(stream);

    stream.write(Image.label);

    Write.U16To(stream, left  );
    Write.U16To(stream, top   );
    Write.U16To(stream, width );
    Write.U16To(stream, height);

    var packedFields = colorTable.map(table -> {
      var packedSize = 0;
      for (var left=table.size(); left > 2; left >>= 1)
        packedSize++;

      return (                     1 << 7    )  // has color table
           | (isInterlaced       ? 1 << 6 : 0)
           | (colorTableIsSorted ? 1 << 5 : 0)
           | (packedSize             << 0    );
    }).orElse(0);
    Write.U8To(stream, packedFields);

    // can't use ifPresent since writeTo throws
    if (colorTable.isPresent()) {
      for (var color : colorTable.get())
        color.writeTo(stream);
    }

    Write.U8To(stream, minimumCodeSize);
    data.writeTo(stream);
  }
}
