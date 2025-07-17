package gif.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import gif.data.exception.UnexpectedEndOfStream;

public record Color(Unsigned.Byte red, Unsigned.Byte green, Unsigned.Byte blue) implements Serializable {
  public Color(Unsigned.Byte red, Unsigned.Byte green, Unsigned.Byte blue) {
    this.red   = Objects.requireNonNull(red  );
    this.green = Objects.requireNonNull(green);
    this.blue  = Objects.requireNonNull(blue );
  }

  public Color(InputStream stream) throws IOException, UnexpectedEndOfStream {
    this(Unsigned.Byte.readFrom(stream), Unsigned.Byte.readFrom(stream), Unsigned.Byte.readFrom(stream));
  }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    red  .writeTo(stream);
    green.writeTo(stream);
    blue .writeTo(stream);
  }
}
