package gif;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import exceptions.InvalidValue;
import exceptions.ParseException;
import exceptions.UnexpectedEndOfStream;

public class Trailer extends Block {
  public final int data = 0x3b;  // expected value

  public Trailer(InputStream stream) throws IOException, ParseException {
    var value = stream.read();
    if (value < 0)
      throw new UnexpectedEndOfStream();
    if (value != data)
      throw new InvalidValue(InvalidValue::formatByte, "trailer block", value, data);
  }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    stream.write(data);
  }
}
