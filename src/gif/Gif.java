package gif;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Optional;

import gif.block.Block;
import gif.block.Extension;
import gif.block.LabeledBlock;
import gif.data.Serializable;
import gif.data.State;
import gif.data.exception.ParseException;

public class Gif implements Serializable {
  public final Block.Header header;
  public final Block.Screen screen;
  public final LabeledBlock[] blocks;
  public final LabeledBlock.Trailer trailer;

  public Gif(InputStream stream) throws IOException, ParseException {
    header = Block.Header.readFrom(stream);
    screen = Block.Screen.readFrom(stream);

    var blocks = new ArrayList<LabeledBlock>();
    var state = new State();
    while (true) {
      switch (LabeledBlock.readFrom(stream, state)) {
        case LabeledBlock.Trailer trailer -> {
          this.trailer = trailer;
          this.blocks = blocks.toArray(LabeledBlock[]::new);

          return;
        }
        case Extension.GraphicControlExtension graphicControlExtension -> {
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
