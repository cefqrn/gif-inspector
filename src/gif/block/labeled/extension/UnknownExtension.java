package gif.block.labeled.extension;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import gif.data.DataBlock;
import gif.data.exception.ParseException;

public record UnknownExtension(
  byte label,
  DataBlock data
) implements Extension {
  public UnknownExtension(byte label, DataBlock data) {
    this.label = label;
    this.data  = Objects.requireNonNull(data);
  }

  public static UnknownExtension readFrom(InputStream stream, byte label) throws IOException, ParseException {
    return new UnknownExtension(label, DataBlock.readFrom(stream));
  }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    stream.write(Extension.label);
    stream.write(label);

    data.writeTo(stream);
  }
}
