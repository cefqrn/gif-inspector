package gif.block.labeled.extension;

import java.io.IOException;
import java.io.InputStream;

import gif.block.labeled.LabeledBlock;
import gif.data.exception.ParseException;
import gif.module.Read;

public interface Extension extends LabeledBlock {
  public static final byte label = 0x21;

  public static Extension readFrom(InputStream stream) throws IOException, ParseException {
    var label = Read.byteFrom(stream);
    return switch (label) {
      case GraphicControlExtension.label -> new GraphicControlExtension(stream);
      default                            -> new UnknownExtension(stream, label);
    };
  }
}
