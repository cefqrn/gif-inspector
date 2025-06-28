package gif.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import gif.exception.ParseException;
import gif.exception.UnexpectedEndOfStream;

public final class DataBlock {
  public static void writeTo(OutputStream stream, byte[][] dataBlock) throws IOException {
    for (var subBlock : dataBlock) {
      stream.write(subBlock.length);
      stream.write(subBlock);
    }

    stream.write(0);
  }

  public static byte[][] readFrom(InputStream stream) throws IOException, ParseException {
    var subBlocks = new ArrayList<byte[]>();
    while (true) {
      var length = stream.read();
      if (length < 0)
        throw new UnexpectedEndOfStream();

      if (length == 0)
        return subBlocks.toArray(byte[][]::new);

      var subBlock = stream.readNBytes(length);
      if (subBlock.length < length)
        throw new UnexpectedEndOfStream();

      subBlocks.add(subBlock);
    }
  }
}
