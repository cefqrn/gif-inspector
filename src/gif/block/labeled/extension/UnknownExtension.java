package gif.block.labeled.extension;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gif.data.DataBlock;
import gif.data.exception.ParseException;

public class UnknownExtension extends Extension {
  public final int label;
  public final DataBlock data;

  public UnknownExtension(InputStream stream, int label) throws IOException, ParseException {
    this.label = label;
    this.data = DataBlock.readFrom(stream);
  }

  @Override
  public int getLabel() { return label; }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    stream.write(Extension.label);
    stream.write(label);

    data.writeTo(stream);
  }
}
