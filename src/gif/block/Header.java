package gif.block;

import java.util.Arrays;
import java.util.Objects;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gif.data.Version;
import gif.data.exception.InvalidValue;
import gif.data.exception.ParseException;
import gif.data.format.ByteArrayFormatter;
import gif.module.Read;

public record Header(Version version) implements Block {
  private static final byte[] EXPECTED_SIGNATURE = "GIF".getBytes();

  public Header(Version version) {
    this.version = Objects.requireNonNull(version);
  }

  public static Header readFrom(InputStream stream) throws IOException, ParseException {
    var buffer = Read.byteArrayFrom(stream, 3);
    if (!Arrays.equals(buffer, EXPECTED_SIGNATURE))
      throw new InvalidValue(ByteArrayFormatter::format, "signature", buffer, EXPECTED_SIGNATURE);

    return new Header(Version.readFrom(stream));
  }

  public byte[] signature() { return EXPECTED_SIGNATURE.clone(); }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    stream.write(EXPECTED_SIGNATURE);
    version.writeTo(stream);
  }
}
