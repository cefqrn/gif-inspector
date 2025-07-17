package gif.block;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gif.data.Unsigned;
import gif.data.Version;
import gif.data.exception.InvalidValue;
import gif.data.exception.ParseException;

public record Header(List<Unsigned.Byte> signature, Version version) implements Block {
  private static final List<Unsigned.Byte> EXPECTED_SIGNATURE = Collections.unmodifiableList(Unsigned.Byte.listFrom("GIF".getBytes()));

  public Header(List<Unsigned.Byte> signature, Version version) {
    InvalidValue.check("signature", signature, EXPECTED_SIGNATURE);

    this.signature = EXPECTED_SIGNATURE;
    this.version   = Objects.requireNonNull(version);
  }

  public static Header readFrom(InputStream stream) throws IOException, ParseException {
    var signature = Unsigned.Byte.listFrom(stream.readNBytes(3));
    var version   = Version.readFrom(stream);

    return new Header(signature, version);
  }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    for (var b : signature)
      b.writeTo(stream);

    version.writeTo(stream);
  }
}
