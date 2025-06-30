package gif.block.labeled.extension;

import java.io.IOException;
import java.io.InputStream;

import gif.block.labeled.LabeledBlock;
import gif.data.exception.ParseException;
import gif.data.exception.UnexpectedEndOfStream;

public abstract class Extension extends LabeledBlock {
  public static final int label = 0x21;

  public static Extension readFrom(InputStream stream) throws IOException, ParseException {
    int labelRead = stream.read();
    if (labelRead < 0)
      throw new UnexpectedEndOfStream();

    return switch (labelRead) {
      case GraphicControlExtension.label -> new GraphicControlExtension(stream);
      default                            -> new UnknownExtension(stream, labelRead);
    };
  }
}
