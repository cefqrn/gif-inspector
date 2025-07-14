package gif.block.labeled.extension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import gif.data.DataBlock;
import gif.data.exception.OutOfBounds;
import gif.data.exception.ParseException;
import gif.module.Write;

public record GraphicControlExtension(
  int disposalMethod,
  boolean waitsForUserInput,
  int delayTime,
  Optional<Integer> transparentColorIndex
) implements Extension {
  public static final byte label = (byte)0xf9;

  public GraphicControlExtension(int disposalMethod, boolean waitsForUserInput, int delayTime, Optional<Integer> transparentColorIndex) {
    if (disposalMethod < 0 || 7 < disposalMethod)
      throw new OutOfBounds("disposal method", disposalMethod, 0, 7);

    if (delayTime < 0 || 0xffff < delayTime)
      throw new OutOfBounds("delay time", delayTime, 0, 0xffff);

    if (transparentColorIndex.isPresent() && (transparentColorIndex.get() < 0 || 0xff < transparentColorIndex.get()))
      throw new OutOfBounds("transparent color index", transparentColorIndex.get(), 0, 0xff);

    this.disposalMethod = disposalMethod;
    this.waitsForUserInput = waitsForUserInput;
    this.delayTime = delayTime;
    this.transparentColorIndex = transparentColorIndex.map(Objects::requireNonNull);
  }

  public static GraphicControlExtension readFrom(InputStream stream) throws IOException, ParseException {
    var datablock = DataBlock.readExpecting(stream, 4);
    var data = datablock.subBlocks().get(0).data;

    var delayTime = (Byte.toUnsignedInt(data.get(2)) << 8) | Byte.toUnsignedInt(data.get(1));

    var packedFields = data.get(0);
    var hasTransparencyIndex = ((packedFields >> 0) & 1) == 1;
    var waitsForUserInput    = ((packedFields >> 1) & 1) == 1;
    var disposalMethod       = ((packedFields >> 2) & 7);

    // var doesn't work here
    Optional<Integer> transparentColorIndex = hasTransparencyIndex
      ? Optional.of(Byte.toUnsignedInt(data.get(3)))
      : Optional.empty();

    return new GraphicControlExtension(disposalMethod, waitsForUserInput, delayTime, transparentColorIndex);
  }

  @Override
  public byte getLabel() { return GraphicControlExtension.label; }

  public void writeTo(OutputStream stream) throws IOException {
    stream.write(Extension.label);
    stream.write(GraphicControlExtension.label);

    var data = new ByteArrayOutputStream(4);

    var packedFields = (transparentColorIndex.isPresent() ? (1 << 0) : 0)
                     | (waitsForUserInput                 ? (1 << 1) : 0)
                     | (disposalMethod                         << 2     );
    Write.U8To(data, packedFields);

    Write.U16To(data, delayTime);
    Write.U8To(data, transparentColorIndex.orElse(0));

    try {
      new DataBlock(List.of(new DataBlock.SubBlock(data.toByteArray()))).writeTo(stream);
    } catch (OutOfBounds e) {}  // unreachable
  }
}
