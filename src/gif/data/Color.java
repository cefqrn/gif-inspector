package gif.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gif.data.exception.UnexpectedEndOfStream;
import gif.module.Read;
import gif.module.Write;

public class Color implements Serializable {
  public final int red;
  public final int green;
  public final int blue;
  public final boolean isTransparent;

  public Color(InputStream stream) throws IOException, UnexpectedEndOfStream {
    red   = Read.U8From(stream);
    green = Read.U8From(stream);
    blue  = Read.U8From(stream);
    isTransparent = false;
  }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    Write.U8To(stream, red);
    Write.U8To(stream, green);
    Write.U8To(stream, blue);
  }
}
