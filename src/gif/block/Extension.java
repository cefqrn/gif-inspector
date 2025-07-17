package gif.block;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import gif.data.DataBlock;
import gif.data.DisposalMethod;
import gif.data.Unsigned;
import gif.data.exception.InvalidValue;
import gif.data.exception.ParseException;

public sealed interface Extension extends LabeledBlock permits UnknownExtension, Extension.GraphicControlExtension {
  public static final byte label = 0x21;

  DataBlock data();

  public static Extension readFrom(InputStream stream) throws IOException, ParseException {
    var label = Unsigned.Byte.readFrom(stream).byteValue();
    var data  = DataBlock.readFrom(stream);
    return switch (Byte.valueOf(label)) {
      case GraphicControlExtension.label -> GraphicControlExtension.from(data);
      case Byte b when BlockType.isLabelForControlBlock(b)          -> new UnknownExtension.UnknownControlExtension         (label, data);
      case Byte b when BlockType.isLabelForGraphicRenderingBlock(b) -> new UnknownExtension.UnknownGraphicRenderingExtension(label, data);
      case Byte b when BlockType.isLabelForSpecialPurposeBlock(b)   -> new UnknownExtension.UnknownSpecialPurposeExtension  (label, data);
      default ->
        throw new Error("unreachable");
    };
  }

  @Override
  default void writeTo(OutputStream stream) throws IOException {
    stream.write(Extension.label);
    stream.write(label());

    data().writeTo(stream);
  }

  record GraphicControlExtension(
    DisposalMethod disposalMethod,
    boolean waitsForUserInput,
    Unsigned.Short delayTime,
    Optional<Unsigned.Byte> transparentColorIndex
  ) implements Extension, BlockType.ControlBlock {
    public static final byte label = (byte)0xf9;

    public GraphicControlExtension(DisposalMethod disposalMethod, boolean waitsForUserInput, Unsigned.Short delayTime, Optional<Unsigned.Byte> transparentColorIndex) {
      this.disposalMethod        = Objects.requireNonNull(disposalMethod);
      this.waitsForUserInput     = waitsForUserInput;
      this.delayTime             = Objects.requireNonNull(delayTime);
      this.transparentColorIndex = transparentColorIndex.map(Objects::requireNonNull);
    }

    public static GraphicControlExtension from(DataBlock data) throws ParseException {
      InvalidValue.check("subblock count", data.subBlocks().size(), 1);

      var bytes = data.subBlocks().get(0).data();
      InvalidValue.check("subblock size", bytes.size(), 4);

      var delayTime = Unsigned.Short.fromBytes(bytes.get(1), bytes.get(2));

      var packedFields = bytes.get(0).intValue();
      var hasTransparencyIndex =                    ((packedFields >> 0) & 1) == 1;
      var waitsForUserInput    =                    ((packedFields >> 1) & 1) == 1;
      var disposalMethod       = DisposalMethod.from((packedFields >> 2) & 7);

      // var doesn't work here
      Optional<Unsigned.Byte> transparentColorIndex = hasTransparencyIndex
        ? Optional.of(bytes.get(3))
        : Optional.empty();

      return new GraphicControlExtension(disposalMethod, waitsForUserInput, delayTime, transparentColorIndex);
    }

    @Override
    public byte label() { return GraphicControlExtension.label; }

    public DataBlock data() {
      var result = new ByteArrayOutputStream(4);

      var packedFields = (transparentColorIndex.isPresent() ? (1 << 0) : 0)
                       | (waitsForUserInput                 ? (1 << 1) : 0)
                       | (disposalMethod.encodedValue()          << 2     );
      result.write(packedFields);

      delayTime.writeTo(result);
      transparentColorIndex.orElse(Unsigned.Byte.ZERO).writeTo(result);

      return new DataBlock(List.of(new DataBlock.SubBlock(Unsigned.Byte.listFrom(result.toByteArray()))));
    }
  }
}
