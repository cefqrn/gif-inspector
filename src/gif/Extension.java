package gif;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import exceptions.ParseException;
import exceptions.UnexpectedEndOfStream;

public abstract class Extension extends LabeledBlock {
  public static final int label = 0x21;

  @Override
  public int getLabel() { return label; }

  public abstract int getExtensionLabel();

  public static Extension readFrom(InputStream stream) throws IOException, ParseException {
    int labelRead = stream.read();
    if (labelRead < 0)
      throw new UnexpectedEndOfStream();

    switch (labelRead) {
    default:
      return new UnknownExtension(stream, labelRead);
    }
  }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    super.writeTo(stream);
    stream.write(getExtensionLabel());
  }
}
