package gif.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import gif.data.exception.InvalidValue;
import gif.data.exception.ParseException;

public record DataBlock(List<SubBlock> subBlocks) implements Serializable {
  public DataBlock(List<SubBlock> subBlocks) {
    subBlocks.stream().forEach(Objects::requireNonNull);
    this.subBlocks = List.copyOf(subBlocks);
  }

  public static DataBlock readFrom(InputStream stream) throws IOException, ParseException {
    var subBlocks = new ArrayList<SubBlock>();
    while (true) {
      var length = Unsigned.Byte.readFrom(stream);
      if (length.equals(Unsigned.Byte.ZERO))
        return new DataBlock(Collections.unmodifiableList(subBlocks));

      subBlocks.add(new SubBlock(Unsigned.Byte.readListFrom(stream, length.intValue())));
    }
  }

  public static DataBlock readExpecting(InputStream stream, int... expectedSizes) throws IOException, ParseException {
    var result = readFrom(stream);

    InvalidValue.check("data block subblock count", result.subBlocks.size(), expectedSizes.length);
    for (var i=0; i < expectedSizes.length; ++i)
      InvalidValue.check("subblock size", result.subBlocks.get(i).data.size(), expectedSizes[i]);

    return result;
  }

  public void writeTo(OutputStream stream) throws IOException {
    for (var subBlock : subBlocks)
      subBlock.writeTo(stream);

    stream.write(0);
  }

  public int totalSize() {
    return subBlocks.stream()
      .mapToInt(subBlock -> subBlock.data.size())
      .sum();
  }

  public record SubBlock(List<Unsigned.Byte> data) implements Serializable {
    public SubBlock(List<Unsigned.Byte> data) {
      data.stream().forEach(Objects::requireNonNull);
      this.data = List.copyOf(data);
    }

    public void writeTo(OutputStream stream) throws IOException {
      stream.write(data.size());
      for (var b : data)
        b.writeTo(stream);
    }
  }
}
