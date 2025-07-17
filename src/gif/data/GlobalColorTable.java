package gif.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import gif.data.exception.OutOfBounds;
import gif.data.exception.UnexpectedEndOfStream;

public record GlobalColorTable(ColorTable colorTable, Unsigned.Byte backgroundColorIndex) {
  public GlobalColorTable(ColorTable colorTable, Unsigned.Byte backgroundColorIndex) {
    OutOfBounds.check("background color index", backgroundColorIndex.intValue(), 0, colorTable.colors().size() - 1);

    this.colorTable           = Objects.requireNonNull(colorTable);
    this.backgroundColorIndex = Objects.requireNonNull(backgroundColorIndex);
  }

  public static GlobalColorTable readFrom(InputStream stream, int packedSize, boolean isSorted, Unsigned.Byte backgroundColorIndex) throws IOException, UnexpectedEndOfStream {
    return new GlobalColorTable(ColorTable.readFrom(stream, packedSize, isSorted), backgroundColorIndex);
  }
}