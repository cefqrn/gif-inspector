package gif;

import java.io.IOException;
import java.io.InputStream;

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

  public boolean isGraphicRenderingBlock() {
    var label = getLabel();
    return 0x00 <= label && label <= 0x7f && !(this instanceof Trailer);
  }

  public boolean isControlBlock() {
    var label = getLabel();
    return 0x80 <= label && label <= 0xf9;
  }

  public boolean isSpecialPurposeBlock() {
    var label = getLabel();
    return 0xfa <= label && label <= 0xff;
  }
}
