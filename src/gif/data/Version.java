package gif.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import gif.data.exception.InvalidValue;
import gif.data.exception.ParseException;
import gif.data.exception.UnexpectedEndOfStream;
import gif.data.format.ByteArrayFormatter;

public enum Version implements Serializable {
  VERSION_87A("87a".getBytes()),
  VERSION_89A("89a".getBytes());

  private final byte[] data;

  private Version(byte[] data) {
    this.data = data.clone();
  }

  public byte[] getData() {
    return data.clone();
  }

  public static Version readFrom(InputStream stream) throws IOException, ParseException {
    var buffer = stream.readNBytes(3);
    if (buffer.length != 3)
      throw new UnexpectedEndOfStream();

    for (var version : Version.values()) {
      if (Arrays.equals(buffer, version.getData()))
        return version;
    }

    throw new InvalidValue(
      ByteArrayFormatter::format,
      "version",
      buffer,
      Arrays.stream(Version.values())
        .map(Version::getData)
        .toArray(byte[][]::new)
    );
  }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    stream.write(getData());
  }
}
