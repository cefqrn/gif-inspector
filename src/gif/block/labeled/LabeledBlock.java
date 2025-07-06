package gif.block.labeled;

import java.io.IOException;
import java.io.InputStream;

import gif.block.Block;
import gif.block.labeled.extension.Extension;
import gif.data.State;
import gif.data.exception.InvalidValue;
import gif.data.exception.ParseException;
import gif.data.exception.UnexpectedEndOfStream;
import gif.data.format.ByteFormatter;

public abstract class LabeledBlock extends Block {
  public abstract byte getLabel();

  public static LabeledBlock readFrom(InputStream stream, State state) throws IOException, ParseException {
    var labelRead = stream.read();
    if (labelRead < 0)
      throw new UnexpectedEndOfStream();

    return switch ((byte)labelRead) {
      case Extension.label -> Extension.readFrom(stream);
      case     Image.label -> new Image(stream, state);
      case   Trailer.label -> new Trailer();
      default ->
        throw new InvalidValue(ByteFormatter::format, "label", (byte)labelRead, Extension.label, Image.label, Trailer.label);
    };
  }

  public boolean isGraphicRenderingBlock() {
    var label = Byte.toUnsignedInt(getLabel());
    return 0x00 <= label && label <= 0x7f && !(this instanceof Trailer);
  }

  public boolean isControlBlock() {
    var label = Byte.toUnsignedInt(getLabel());
    return 0x80 <= label && label <= 0xf9;
  }

  public boolean isSpecialPurposeBlock() {
    var label = Byte.toUnsignedInt(getLabel());
    return 0xfa <= label && label <= 0xff;
  }
}
