package gif;

import java.io.IOException;
import java.io.OutputStream;

public class Trailer extends LabeledBlock {
  public static final int label = 0x3b;

  @Override
  public int getLabel() { return Trailer.label; }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    stream.write(Trailer.label);
  }
}
