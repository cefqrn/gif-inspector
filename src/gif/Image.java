package gif;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Optional;

import exceptions.ParseException;
import serializable.LittleEndian;

public class Image extends LabeledBlock {
  static final int label = 0x2c;

  public final int left;
  public final int top;
  public final int width;
  public final int height;
  protected final Optional<Color[]> colorTable;
  public final boolean isInterlaced;
  public final boolean colorTableIsSorted;
  public final int minimumCodeSize;
  protected final byte[][] data;
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

    Color[] table = null;
    if (hasColorTable) {
      var size         = 1 << (((packedFields >> 0) & 7) + 1);

      table = new Color[size];
      for (var i=0; i < size; ++i)
        table[i] = new Color(stream);
    }
    colorTable = Optional.ofNullable(table);

    minimumCodeSize = LittleEndian.readU8From(stream);
    data = DataBlock.readFrom(stream);

    graphicControlExtension = state.graphicControlExtension;
  }

  @Override
  public int getLabel() { return label; }

  public byte[][] getData() {
    return Arrays.stream(data)
      .map(byte[]::clone)
      .toArray(byte[][]::new);
  }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    // can't use ifPresent since writeTo throws
    if (graphicControlExtension.isPresent())
      graphicControlExtension.get().writeTo(stream);

    super.writeTo(stream);

    LittleEndian.writeU16To(stream, left  );
    LittleEndian.writeU16To(stream, top   );
    LittleEndian.writeU16To(stream, width );
    LittleEndian.writeU16To(stream, height);

    var packedFields = colorTable.map(table -> {
      var packedSize = 0;
      for (var left=table.length; left > 2; left >>= 1)
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
    DataBlock.writeTo(stream, data);
  }
}
