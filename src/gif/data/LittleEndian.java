package gif.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gif.data.exception.ParseException;
import gif.data.exception.UnexpectedEndOfStream;

public class LittleEndian {
  public static void writeU16To(OutputStream stream, int x) throws IOException {
    stream.write(x);
    stream.write(x >> 8);
  }

  public static int readU16From(InputStream stream) throws IOException, ParseException {
    var a = stream.read();
    var b = stream.read();
    if (b < 0)
      throw new UnexpectedEndOfStream();

    return (b << 8) | a;
  }

  public static void writeU8To(OutputStream stream, int x) throws IOException {
    stream.write(x);
  }

  public static int readU8From(InputStream stream) throws IOException, ParseException {
    var a = stream.read();
    if (a < 0)
      throw new UnexpectedEndOfStream();

    return a;
  }
}
