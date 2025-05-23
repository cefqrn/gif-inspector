package gif;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import exceptions.ParseException;

public class UnknownExtension extends Extension {
  final int extensionLabel;
  protected final byte[][] data;

  public UnknownExtension(InputStream stream, int extensionLabel) throws IOException, ParseException {
    this.extensionLabel = extensionLabel;
    this.data = DataBlock.readFrom(stream);
  }

  public byte[][] getData() {
    return Arrays.stream(data)
      .map(byte[]::clone)
      .toArray(byte[][]::new);
  }

  @Override
  public int getExtensionLabel() {
    return extensionLabel;
  }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    super.writeTo(stream);
    DataBlock.writeTo(stream, data);
  }
}
