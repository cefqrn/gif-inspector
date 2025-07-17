package gif.module;

import java.io.IOException;
import java.io.InputStream;

import gif.data.exception.UnexpectedEndOfStream;

/** Module for reading values from a little-endian InputStream */
public final class Read {
  private Read() {
    throw new UnsupportedOperationException("cannot instantiate module");
  }

  public static byte[] byteArrayFrom(InputStream stream, int length) throws IOException, UnexpectedEndOfStream {
    if (length < 0)
      throw new IllegalArgumentException("length must be nonnegative (got " + length + ")");

    var result = stream.readNBytes(length);
    if (result.length < length)
      throw new UnexpectedEndOfStream();

    return result;
  }
}
