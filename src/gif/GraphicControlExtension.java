package gif;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import exceptions.InvalidValue;
import exceptions.ParseException;
import serializable.LittleEndian;

public class GraphicControlExtension extends Extension {
  public static final int label = 0xf9;

  public final int disposalMethod;
  public final boolean waitsForUserInput;
  public final int delayTime;
  public final Optional<Integer> transparentColorIndex;

  public GraphicControlExtension(InputStream stream) throws IOException, ParseException {
    var blocks = DataBlock.readFrom(stream);
    if (blocks.length != 1)
      throw new InvalidValue("graphic control extension data subblock count", blocks.length, 1);

    var data = blocks[0];
    if (data.length != 4)
      throw new InvalidValue("graphic control extension data length", data.length, 4);

    delayTime = (data[2] << 8) | data[1];

    var packedFields = data[0];
    var hasTransparencyIndex = ((packedFields >> 0) & 1) == 1;
    waitsForUserInput        = ((packedFields >> 1) & 1) == 1;
    disposalMethod           = ((packedFields >> 2) & 7);

    transparentColorIndex = hasTransparencyIndex ? Optional.of(Byte.toUnsignedInt(data[3])) : Optional.empty();
  }

  @Override
  public int getLabel() { return GraphicControlExtension.label; }

  public void writeTo(OutputStream stream) throws IOException {
    stream.write(Extension.label);
    stream.write(GraphicControlExtension.label);

    var data = new ByteArrayOutputStream(4);

    var packedFields = (transparentColorIndex.isPresent() ? (1 << 0) : 0)
                     |                 (waitsForUserInput ? (1 << 1) : 0)
                     |                         (disposalMethod << 2);
    LittleEndian.writeU8To(data, packedFields);

    LittleEndian.writeU16To(data, delayTime);
    LittleEndian.writeU8To(data, transparentColorIndex.orElse(0));

    DataBlock.writeTo(stream, new byte[][]{data.toByteArray()});
  }
}
