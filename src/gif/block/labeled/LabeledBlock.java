package gif.block.labeled;

import java.io.IOException;
import java.io.InputStream;

import gif.block.Block;
import gif.block.labeled.extension.Extension;
import gif.data.State;
import gif.data.Unsigned;
import gif.data.exception.InvalidValue;
import gif.data.exception.ParseException;
import gif.data.format.ByteFormatter;

public interface LabeledBlock extends Block {
  byte label();

  public static LabeledBlock readFrom(InputStream stream, State state) throws IOException, ParseException {
    var label = Unsigned.Byte.readFrom(stream).byteValue();
    return switch (label) {
      case Extension.label -> Extension.readFrom(stream);
      case     Image.label -> Image.readFrom(stream, state);
      case   Trailer.label -> new Trailer();
      default ->
        throw new InvalidValue(ByteFormatter::format, "label", label, Extension.label, Image.label, Trailer.label);
    };
  }

  default boolean isGraphicRenderingBlock() {
    var label = Byte.toUnsignedInt(label());
    return 0x00 <= label && label <= 0x7f;
  }

  default boolean isControlBlock() {
    var label = Byte.toUnsignedInt(label());
    return 0x80 <= label && label <= 0xf9;
  }

  default boolean isSpecialPurposeBlock() {
    var label = Byte.toUnsignedInt(label());
    return 0xfa <= label && label <= 0xff;
  }
}
