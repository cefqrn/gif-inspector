package gif.block;

import java.util.Arrays;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gif.data.Version;
import gif.data.exception.InvalidValue;
import gif.data.exception.ParseException;
import gif.data.exception.UnexpectedEndOfStream;
import gif.data.format.ByteArray;

public class Header extends Block {
  public final Version version;

  protected static final byte[] EXPECTED_SIGNATURE = "GIF".getBytes();

  public Header(InputStream stream) throws IOException, ParseException {
    var buffer = stream.readNBytes(3);
    if (buffer.length != 3)
      throw new UnexpectedEndOfStream();
    if (!Arrays.equals(buffer, EXPECTED_SIGNATURE))
      throw new InvalidValue(ByteArray::format, "signature", buffer, EXPECTED_SIGNATURE);

    version = Version.readFrom(stream);
  }

  public byte[] getSignature() { return EXPECTED_SIGNATURE.clone(); }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    stream.write(EXPECTED_SIGNATURE);
    version.writeTo(stream);
  }
}
