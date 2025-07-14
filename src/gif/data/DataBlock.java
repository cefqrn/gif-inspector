package gif.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import gif.data.exception.InvalidValue;
import gif.data.exception.OutOfBounds;
import gif.data.exception.ParseException;
import gif.module.Read;

public record DataBlock(List<SubBlock> subBlocks) implements Serializable {
  public DataBlock(List<SubBlock> subBlocks) {
    subBlocks.stream().forEach(Objects::requireNonNull);
    this.subBlocks = List.copyOf(subBlocks);
  }

  public static DataBlock readFrom(InputStream stream) throws IOException, ParseException {
    var subBlocks = new ArrayList<SubBlock>();
    while (true) {
      var length = Read.U8From(stream);
      if (length == 0)
        return new DataBlock(Collections.unmodifiableList(subBlocks));

      subBlocks.add(new SubBlock(Read.byteArrayFrom(stream, length)));
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

  public static final class SubBlock implements Serializable {
    public final List<Byte> data;

    public SubBlock(byte[] data) throws OutOfBounds {
      var size = OutOfBounds.check("subblock size", data.length, 1, 0xff);

      var list = new ArrayList<Byte>(size);
      for (var b : data)
        list.add(b);

      this.data = Collections.unmodifiableList(list);
    }

    public void writeTo(OutputStream stream) throws IOException {
      stream.write(data.size());
      for (var b : data)
        stream.write(b);
    }
  }
}
