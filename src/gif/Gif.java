package gif;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import exceptions.ParseException;
import serializable.Serializable;

public class Gif implements Serializable {
  public final Header header;
  public final Screen screen;
  public final LabeledBlock[] blocks;
  public final Trailer trailer;

  public Gif(InputStream stream) throws IOException, ParseException {
    header = new Header(stream);
    screen = new Screen(stream);

    var blocks = new ArrayList<LabeledBlock>();
    while (true) {
      var block = LabeledBlock.readFrom(stream);
      if (block instanceof Trailer) {
        trailer = (Trailer)block;
        break;
      }

      blocks.add(block);
    }

    this.blocks = blocks.toArray(LabeledBlock[]::new);
  }

  public LabeledBlock[] getBlocks() { return blocks.clone(); }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    header.writeTo(stream);
    screen.writeTo(stream);

    for (var block : blocks)
      block.writeTo(stream);

    trailer.writeTo(stream);
  }
}
