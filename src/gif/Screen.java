package gif;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import exceptions.ParseException;
import serializable.LittleEndian;

public class Screen extends Block {
  public int width;
  public int height;
  public int colorResolution;
  public boolean globalColorTableIsSorted;
  public Color[] globalColorTable;
  public int backgroundColorIndex;
  public int pixelAspectRatio;

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    LittleEndian.writeU16To(stream, width);
    LittleEndian.writeU16To(stream, height);

    var packedFields = 0;
    var hasGlobalColorTable = globalColorTable != null;
    if (hasGlobalColorTable) {
      int packedSize = 0;
      for (var left=globalColorTable.length; left > 2; left >>= 1)
        packedSize++;

      packedFields =                            (1 << 7)      // has global color table
                   |              (colorResolution << 4)
                   | (globalColorTableIsSorted ? 1 << 3 : 0)
                   |                   (packedSize << 0);
    }
    LittleEndian.writeU8To(stream, packedFields);

    LittleEndian.writeU8To(stream, backgroundColorIndex);
    LittleEndian.writeU8To(stream, pixelAspectRatio);

    if (hasGlobalColorTable) {
      for (var color : globalColorTable)
        color.writeTo(stream);
    }
  }

  @Override
  public void readFrom(InputStream stream) throws IOException, ParseException {
    width = LittleEndian.readU16From(stream);
    height = LittleEndian.readU16From(stream);

    var packedFields = LittleEndian.readU8From(stream);

    backgroundColorIndex = LittleEndian.readU8From(stream);
    pixelAspectRatio = LittleEndian.readU8From(stream);

    var hasGlobalColorTable  =        (packedFields >> 7) == 1;
    if (!hasGlobalColorTable)
      return;

    colorResolution          =        (packedFields >> 4) & 7;
    globalColorTableIsSorted =       ((packedFields >> 3) & 1) == 1;

    var size                 = 1 << (((packedFields >> 0) & 7) + 1);
    globalColorTable = new Color[size];
    for (var i=0; i < size; ++i) {
      globalColorTable[i] = new Color();
      globalColorTable[i].readFrom(stream);
    }
  }
}
