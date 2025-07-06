package gif;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Optional;

import gif.block.Header;
import gif.block.Screen;
import gif.block.labeled.LabeledBlock;
import gif.block.labeled.Trailer;
import gif.block.labeled.extension.GraphicControlExtension;
import gif.data.Serializable;
import gif.data.State;
import gif.data.exception.ParseException;

public class Gif implements Serializable {
  public final Header header;
  public final Screen screen;
  public final LabeledBlock[] blocks;
  public final Trailer trailer;

  public Gif(InputStream stream) throws IOException, ParseException {
    header = new Header(stream);
    screen = new Screen(stream);

    var blocks = new ArrayList<LabeledBlock>();
    var state = new State();
    while (true) {
      switch (LabeledBlock.readFrom(stream, state)) {
        case Trailer trailer -> {
          this.trailer = trailer;
          this.blocks = blocks.toArray(LabeledBlock[]::new);

          return;
        }
        case GraphicControlExtension graphicControlExtension -> {
          state.graphicControlExtension = Optional.of(graphicControlExtension);
        }
        case LabeledBlock block -> {
          if (block.isGraphicRenderingBlock())
            state = new State();  // clear control blocks

          blocks.add(block);
        }
      }
    }
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
