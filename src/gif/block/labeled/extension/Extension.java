package gif.block.labeled.extension;

import java.io.IOException;
import java.io.InputStream;

import gif.block.labeled.LabeledBlock;
import gif.data.Unsigned;
import gif.data.exception.ParseException;

public interface Extension extends LabeledBlock {
  public static final byte label = 0x21;

  public static Extension readFrom(InputStream stream) throws IOException, ParseException {
    var label = Unsigned.Byte.readFrom(stream).byteValue();
    return switch (label) {
      case GraphicControlExtension.label -> GraphicControlExtension.readFrom(stream);
      default                            -> UnknownExtension.readFrom(stream, label);
    };
  }
}
