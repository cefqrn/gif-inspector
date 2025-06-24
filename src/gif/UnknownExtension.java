package gif;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import exceptions.ParseException;

public class UnknownExtension extends Extension {
  public final int label;
  protected final byte[][] data;

  public UnknownExtension(InputStream stream, int label) throws IOException, ParseException {
    this.label = label;
    this.data = DataBlock.readFrom(stream);
  }

  public byte[][] getData() {
    return Arrays.stream(data)
      .map(byte[]::clone)
      .toArray(byte[][]::new);
  }

  @Override
  public int getLabel() { return label; }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    stream.write(Extension.label);
    stream.write(label);

    DataBlock.writeTo(stream, data);
  }
}
