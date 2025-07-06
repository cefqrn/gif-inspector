package gif.module;

import java.io.IOException;
import java.io.OutputStream;

/** Module for writing values to a little-endian OutputStream */
public final class Write {
  private Write() {
    throw new UnsupportedOperationException("cannot instantiate module");
  }

  public static void U8To(OutputStream stream, int x) throws IOException {
    stream.write(x);
  }

  public static void U16To(OutputStream stream, int x) throws IOException {
    stream.write(x);
    stream.write(x >> 8);
  }
}
