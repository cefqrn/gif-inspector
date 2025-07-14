package gif.block.labeled.extension;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gif.data.DataBlock;
import gif.data.exception.ParseException;

public class UnknownExtension implements Extension {
  public final byte label;
  public final DataBlock data;

  public UnknownExtension(InputStream stream, byte label) throws IOException, ParseException {
    this.label = label;
    this.data = DataBlock.readFrom(stream);
  }

  @Override
  public byte getLabel() { return label; }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    stream.write(Extension.label);
    stream.write(label);

    data.writeTo(stream);
  }
}
