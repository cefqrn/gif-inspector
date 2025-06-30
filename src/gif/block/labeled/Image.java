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
import gif.data.LittleEndian;
import gif.data.State;
import gif.data.exception.ParseException;

public class Image extends LabeledBlock {
  public static final int label = 0x2c;

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
    left   = LittleEndian.readU16From(stream);
    top    = LittleEndian.readU16From(stream);
    width  = LittleEndian.readU16From(stream);
    height = LittleEndian.readU16From(stream);

    var packedFields = LittleEndian.readU8From(stream);

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

    minimumCodeSize = LittleEndian.readU8From(stream);
    data = DataBlock.readFrom(stream);

    graphicControlExtension = state.graphicControlExtension;
  }

  @Override
  public int getLabel() { return Image.label; }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    // can't use ifPresent since writeTo throws
    if (graphicControlExtension.isPresent())
      graphicControlExtension.get().writeTo(stream);

    stream.write(Image.label);

    LittleEndian.writeU16To(stream, left  );
    LittleEndian.writeU16To(stream, top   );
    LittleEndian.writeU16To(stream, width );
    LittleEndian.writeU16To(stream, height);

    var packedFields = colorTable.map(table -> {
      var packedSize = 0;
      for (var left=table.size(); left > 2; left >>= 1)
        packedSize++;

      return (                     1 << 7    )  // has color table
           | (isInterlaced       ? 1 << 6 : 0)
           | (colorTableIsSorted ? 1 << 5 : 0)
           | (packedSize             << 0    );
    }).orElse(0);
    LittleEndian.writeU8To(stream, packedFields);

    // can't use ifPresent since writeTo throws
    if (colorTable.isPresent()) {
      for (var color : colorTable.get())
        color.writeTo(stream);
    }

    LittleEndian.writeU8To(stream, minimumCodeSize);
    data.writeTo(stream);
  }
}
