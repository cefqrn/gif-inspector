package gif;

import serializable.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Trailer extends Block {
  static final int EXPECTED_DATA = 0x3b;

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    stream.write(EXPECTED_DATA);
  }

  @Override
  public void readFrom(InputStream stream) throws IOException, ParseException {
    var data = stream.read();
    if (data < 0)
      throw new UnexpectedEndOfStream();
    if (data != EXPECTED_DATA)
      throw new InvalidValue(InvalidValue::formatByte, "trailer block", EXPECTED_DATA, data);
  }
}
