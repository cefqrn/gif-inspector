package gif.block.labeled;

import java.io.IOException;
import java.io.OutputStream;

public record Trailer() implements LabeledBlock {
  public static final byte label = 0x3b;

  @Override
  public byte label() { return Trailer.label; }

  @Override
  public boolean isGraphicRenderingBlock() {
    return false;
  }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    stream.write(Trailer.label);
  }
}
