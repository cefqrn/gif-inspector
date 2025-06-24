package gif;

import java.io.IOException;
import java.io.InputStream;

import exceptions.ParseException;
import exceptions.UnexpectedEndOfStream;

public abstract class Extension extends LabeledBlock {
  public static final int label = 0x21;

  public static Extension readFrom(InputStream stream) throws IOException, ParseException {
    int labelRead = stream.read();
    if (labelRead < 0)
      throw new UnexpectedEndOfStream();

    switch (labelRead) {
    case GraphicControlExtension.label:
      return new GraphicControlExtension(stream);
    default:
      return new UnknownExtension(stream, labelRead);
    }
  }
}
