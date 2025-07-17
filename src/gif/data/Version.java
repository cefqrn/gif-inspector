package gif.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import gif.data.exception.InvalidValue;
import gif.data.exception.ParseException;

public enum Version implements Serializable {
  VERSION_87A(Unsigned.Byte.listFrom("87a".getBytes())),
  VERSION_89A(Unsigned.Byte.listFrom("89a".getBytes()));

  private final List<Unsigned.Byte> data;

  private Version(List<Unsigned.Byte> data) {
    this.data = List.copyOf(data);
  }

  public List<Unsigned.Byte> data() { return data; }

  public static Version readFrom(InputStream stream) throws IOException, ParseException {
    var data = Unsigned.Byte.listFrom(stream.readNBytes(3));
    for (var version : Version.values())
      if (data.equals(version.data))
        return version;

    throw new InvalidValue(
      "version",
      data,
      Arrays.stream(Version.values())
        .map(Version::data)
        .toArray(List[]::new)
    );
  }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    for (var b : data)
      b.writeTo(stream);
  }
}
