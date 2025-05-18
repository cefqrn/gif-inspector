package gif;

import java.util.Arrays;

import exceptions.InvalidValue;
import exceptions.ParseException;
import exceptions.UnexpectedEndOfStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Header extends Block {
  public final Version version;

  protected static final byte[] EXPECTED_SIGNATURE = "GIF".getBytes();

  public Header(InputStream stream) throws IOException, ParseException {
    var buffer = new byte[3];

    if (stream.read(buffer) < buffer.length)
      throw new UnexpectedEndOfStream();
    if (!Arrays.equals(buffer, EXPECTED_SIGNATURE))
      throw new InvalidValue(InvalidValue::formatByteArray, "signature", buffer, EXPECTED_SIGNATURE);

    if (stream.read(buffer) < buffer.length)
      throw new UnexpectedEndOfStream();

    version = Arrays.stream(Version.values())
      .filter(x -> Arrays.equals(buffer, x.getData()))
      .findFirst()
      .orElseThrow(() ->
        new InvalidValue(
          InvalidValue::formatByteArray,
          "version",
          buffer,
          Arrays.stream(Version.values()).map(Version::getData).toArray(byte[][]::new)
        ));
  }

  public byte[] getSignature() { return EXPECTED_SIGNATURE.clone(); }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    stream.write(EXPECTED_SIGNATURE);
    stream.write(version.getData());
  }
}
