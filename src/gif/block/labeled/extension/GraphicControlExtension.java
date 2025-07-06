package gif.block.labeled.extension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

import gif.data.DataBlock;
import gif.data.exception.OutOfBounds;
import gif.data.exception.ParseException;
import gif.module.Write;

public class GraphicControlExtension extends Extension {
  public static final byte label = (byte)0xf9;

  public final int disposalMethod;
  public final boolean waitsForUserInput;
  public final int delayTime;
  public final Optional<Integer> transparentColorIndex;

  public GraphicControlExtension(InputStream stream) throws IOException, ParseException {
    var datablock = DataBlock.readExpecting(stream, 4);
    var data = datablock.subBlocks().get(0).data;

    delayTime = (data.get(2) << 8) | data.get(1);

    var packedFields = data.get(0);
    var hasTransparencyIndex = ((packedFields >> 0) & 1) == 1;
    waitsForUserInput        = ((packedFields >> 1) & 1) == 1;
    disposalMethod           = ((packedFields >> 2) & 7);

    transparentColorIndex = hasTransparencyIndex ? Optional.of(Byte.toUnsignedInt(data.get(3))) : Optional.empty();
  }

  @Override
  public byte getLabel() { return GraphicControlExtension.label; }

  public void writeTo(OutputStream stream) throws IOException {
    stream.write(Extension.label);
    stream.write(GraphicControlExtension.label);

    var data = new ByteArrayOutputStream(4);

    var packedFields = (transparentColorIndex.isPresent() ? (1 << 0) : 0)
                     |                 (waitsForUserInput ? (1 << 1) : 0)
                     |                         (disposalMethod << 2);
    Write.U8To(data, packedFields);

    Write.U16To(data, delayTime);
    Write.U8To(data, transparentColorIndex.orElse(0));

    try {
      new DataBlock(List.of(new DataBlock.SubBlock(data.toByteArray()))).writeTo(stream);
    } catch (OutOfBounds e) {}  // unreachable
  }
}
