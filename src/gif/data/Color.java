package gif.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gif.data.exception.UnexpectedEndOfStream;
import gif.module.Read;
import gif.module.Write;

public record Color(int red, int green, int blue) implements Serializable {
  public Color(int red, int green, int blue) {
    if (red   < 0 || red   > 255) throw new IllegalArgumentException("color values must be between 0 and 255 (got red="   + red   + ")");
    if (green < 0 || green > 255) throw new IllegalArgumentException("color values must be between 0 and 255 (got green=" + green + ")");
    if (blue  < 0 || blue  > 255) throw new IllegalArgumentException("color values must be between 0 and 255 (got blue="  + blue  + ")");

    this.red   = red;
    this.green = green;
    this.blue  = blue;
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
