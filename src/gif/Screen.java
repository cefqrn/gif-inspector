package gif;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import exceptions.ParseException;
import serializable.LittleEndian;

public class Screen extends Block {
  public final int width;
  public final int height;
  public final int pixelAspectRatio;
  public final int colorResolution;
  protected final Optional<Color[]> globalColorTable;
  public final boolean globalColorTableIsSorted;
  public final int backgroundColorIndex;

  public Screen(InputStream stream) throws IOException, ParseException {
    width = LittleEndian.readU16From(stream);
    height = LittleEndian.readU16From(stream);

    var packedFields = LittleEndian.readU8From(stream);

    backgroundColorIndex = LittleEndian.readU8From(stream);
    pixelAspectRatio = LittleEndian.readU8From(stream);

    var hasGlobalColorTable  =        (packedFields >> 7) == 1;
    colorResolution          =        (packedFields >> 4) & 7;
    globalColorTableIsSorted =       ((packedFields >> 3) & 1) == 1;

    Color[] table = null;
    if (hasGlobalColorTable) {
      var size               = 1 << (((packedFields >> 0) & 7) + 1);

      table = new Color[size];
      for (var i=0; i < size; ++i)
        table[i] = new Color(stream);
    }
    globalColorTable = Optional.ofNullable(table);
  }

  public Optional<Color[]> getGlobalColorTable() {
    return globalColorTable.map(Color[]::clone);
  }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    LittleEndian.writeU16To(stream, width);
    LittleEndian.writeU16To(stream, height);

    var packedFields =    (colorResolution << 4)
                     | globalColorTable.map(table -> {
      int packedSize = 0;
      for (var left=table.length; left > 2; left >>= 1)
        packedSize++;

      return                            (1 << 7)      // has global color table
           | (globalColorTableIsSorted ? 1 << 3 : 0)
           |                   (packedSize << 0);
    }).orElse(0);
    LittleEndian.writeU8To(stream, packedFields);

    LittleEndian.writeU8To(stream, backgroundColorIndex);
    LittleEndian.writeU8To(stream, pixelAspectRatio);

    // can't use ifPresent since Color.writeTo throws
    if (globalColorTable.isPresent()) {
      for (var color : globalColorTable.get())
        color.writeTo(stream);
    }
  }
}
