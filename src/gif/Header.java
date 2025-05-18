package gif;

import java.util.Arrays;

import exceptions.InvalidValue;
import exceptions.ParseException;
import exceptions.UnexpectedEndOfStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Header extends Block {
  byte[] signature = new byte[3];
  Version version;

  private static final byte[] EXPECTED_SIGNATURE = "GIF".getBytes();

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    stream.write(signature);
    stream.write(version.getData());
  }

  @Override
  public void readFrom(InputStream stream) throws IOException, ParseException {
    if (stream.read(signature) < signature.length)
      throw new UnexpectedEndOfStream();
    if (!Arrays.equals(signature, EXPECTED_SIGNATURE))
      throw new InvalidValue(InvalidValue::formatByteArray, "signature", signature, EXPECTED_SIGNATURE);

    var buffer = new byte[3];
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
}
