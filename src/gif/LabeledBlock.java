package gif;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import exceptions.InvalidValue;
import exceptions.ParseException;
import exceptions.UnexpectedEndOfStream;

public abstract class LabeledBlock extends Block {
  public abstract int getLabel();

  public static LabeledBlock readFrom(InputStream stream, State state) throws IOException, ParseException {
    int labelRead = stream.read();
    if (labelRead < 0)
      throw new UnexpectedEndOfStream();

    switch (labelRead) {
    case Extension.label:
      return Extension.readFrom(stream);
    case Image.label:
      return new Image(stream, state);
    case Trailer.label:
      return new Trailer();
    default:
      throw new InvalidValue(InvalidValue::formatByte, "label", labelRead, Extension.label, Image.label, Trailer.label);
    }
  }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    stream.write(getLabel());
  }
}
