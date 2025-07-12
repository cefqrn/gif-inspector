package gif.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gif.data.exception.UnexpectedEndOfStream;

public record ColorTable(List<Color> colors, boolean isSorted) implements Serializable {
  public ColorTable(List<Color> colors, boolean isSorted) {
    var sizeIsAValidPowerOf2 = false;
    for (var i=1; i <= 8; ++i)
      if (colors.size() == (1 << i))
        sizeIsAValidPowerOf2 = true;

    if (!sizeIsAValidPowerOf2)
      throw new IllegalArgumentException(
        "color table size must be a power of 2 between 2 and 256 (got " + colors.size() + ")");

    this.colors = List.copyOf(colors);
    this.isSorted = isSorted;
  }

  public static ColorTable readFrom(InputStream stream, int packedSize, boolean isSorted) throws IOException, UnexpectedEndOfStream {
    var size = 1 << (packedSize + 1);
    var table = new ArrayList<Color>(size);
    for (var i=0; i < size; ++i)
      table.add(new Color(stream));

    return new ColorTable(Collections.unmodifiableList(table), isSorted);
  }

  public int packedSize() {
    for (var i=1; i <= 8; ++i)
      if (colors.size() == (1 << i))
        return i - 1;

    throw new Error("unreachable");
  }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    for (var color : colors())
      color.writeTo(stream);
  }
}
