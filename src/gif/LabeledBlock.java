package gif;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import exceptions.InvalidValue;
import exceptions.ParseException;
import exceptions.UnexpectedEndOfStream;

public abstract class LabeledBlock extends Block {
  private final int label;

  public LabeledBlock(int label) {
    this.label = label;
  }

  public static LabeledBlock readFrom(InputStream stream) throws IOException, ParseException {
    int labelRead = stream.read();
    if (labelRead < 0)
      throw new UnexpectedEndOfStream();

    switch (labelRead) {
    case Trailer.label:
      return new Trailer();
    default:
      throw new InvalidValue(InvalidValue::formatByte, "label", labelRead, Trailer.label);
    }
  }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    stream.write(label);
  }
}
