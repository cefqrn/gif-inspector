package gif.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gif.data.exception.InvalidValue;
import gif.data.exception.OutOfBounds;
import gif.data.exception.ParseException;
import gif.data.exception.UnexpectedEndOfStream;

public record DataBlock(List<SubBlock> subBlocks) implements Serializable {
  public static DataBlock readFrom(InputStream stream) throws IOException, ParseException {
    var subBlocks = new ArrayList<SubBlock>();
    while (true) {
      var length = stream.read();
      if (length < 0)
        throw new UnexpectedEndOfStream();

      if (length == 0)
        return new DataBlock(Collections.unmodifiableList(subBlocks));

      var subBlock = new SubBlock(stream.readNBytes(length));
      if (subBlock.data.size() < length)
        throw new UnexpectedEndOfStream();

      subBlocks.add(subBlock);
    }
  }

  public static DataBlock readExpecting(InputStream stream, int... expectedSizes) throws IOException, ParseException {
    var result = readFrom(stream);
    if (result.subBlocks.size() != expectedSizes.length)
      throw new InvalidValue("data block subblock count", result.subBlocks.size(), expectedSizes.length);

    for (var i=0; i < expectedSizes.length; ++i) {
      var subBlockSize = result.subBlocks.get(i).data.size();
      if (subBlockSize != expectedSizes[i])
        throw new InvalidValue("subblock size", subBlockSize, expectedSizes[i]);
    }

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
      var size = data.length;
      if (size < 1 || 255 < size)
        throw new OutOfBounds("SubBlock size", size, 1, 255);

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
