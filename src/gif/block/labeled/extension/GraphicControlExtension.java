package gif.block.labeled.extension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import gif.data.DataBlock;
import gif.data.DisposalMethod;
import gif.data.Unsigned;
import gif.data.exception.OutOfBounds;
import gif.data.exception.ParseException;

public record GraphicControlExtension(
  DisposalMethod disposalMethod,
  boolean waitsForUserInput,
  Unsigned.Short delayTime,
  Optional<Unsigned.Byte> transparentColorIndex
) implements Extension {
  public static final byte label = (byte)0xf9;

  public GraphicControlExtension(DisposalMethod disposalMethod, boolean waitsForUserInput, Unsigned.Short delayTime, Optional<Unsigned.Byte> transparentColorIndex) {
    this.disposalMethod        = Objects.requireNonNull(disposalMethod);
    this.waitsForUserInput     = waitsForUserInput;
    this.delayTime             = Objects.requireNonNull(delayTime);
    this.transparentColorIndex = transparentColorIndex.map(Objects::requireNonNull);
  }

  public static GraphicControlExtension readFrom(InputStream stream) throws IOException, ParseException {
    var datablock = DataBlock.readExpecting(stream, 4);
    var data = datablock.subBlocks().get(0).data();

    var delayTime = Unsigned.Short.fromBytes(data.get(1), data.get(2));

    var packedFields = data.get(0).intValue();
    var hasTransparencyIndex =                    ((packedFields >> 0) & 1) == 1;
    var waitsForUserInput    =                    ((packedFields >> 1) & 1) == 1;
    var disposalMethod       = DisposalMethod.from((packedFields >> 2) & 7);

    // var doesn't work here
    Optional<Unsigned.Byte> transparentColorIndex = hasTransparencyIndex
      ? Optional.of(data.get(3))
      : Optional.empty();

    return new GraphicControlExtension(disposalMethod, waitsForUserInput, delayTime, transparentColorIndex);
  }

  @Override
  public byte label() { return GraphicControlExtension.label; }

  public void writeTo(OutputStream stream) throws IOException {
    stream.write(Extension.label);
    stream.write(GraphicControlExtension.label);

    var data = new ByteArrayOutputStream(4);

    var packedFields = (transparentColorIndex.isPresent() ? (1 << 0) : 0)
                     | (waitsForUserInput                 ? (1 << 1) : 0)
                     | (disposalMethod.encodedValue()          << 2     );
    data.write(packedFields);

    delayTime.writeTo(data);
    transparentColorIndex.orElse(Unsigned.Byte.ZERO).writeTo(data);

    var bytes = new ArrayList<Unsigned.Byte>(data.size());
    for (var b : data.toByteArray())
      bytes.add(new Unsigned.Byte(Byte.toUnsignedInt(b)));

    try {
      new DataBlock(List.of(new DataBlock.SubBlock(bytes))).writeTo(stream);
    } catch (OutOfBounds e) {}  // unreachable
  }
}
