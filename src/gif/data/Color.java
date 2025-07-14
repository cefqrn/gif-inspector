package gif.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gif.data.exception.OutOfBounds;
import gif.data.exception.UnexpectedEndOfStream;
import gif.module.Read;
import gif.module.Write;

public record Color(int red, int green, int blue) implements Serializable {
  public Color(int red, int green, int blue) {
    this.red   = OutOfBounds.check("red",   red,   0, 0xff);
    this.green = OutOfBounds.check("green", green, 0, 0xff);
    this.blue  = OutOfBounds.check("blue",  blue,  0, 0xff);
  }

  public Color(InputStream stream) throws IOException, UnexpectedEndOfStream {
    this(Read.U8From(stream), Read.U8From(stream), Read.U8From(stream));
  }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    Write.U8To(stream, red);
    Write.U8To(stream, green);
    Write.U8To(stream, blue);
  }
}
