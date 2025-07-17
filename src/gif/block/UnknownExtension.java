package gif.block;

import java.util.Objects;

import gif.data.DataBlock;

public sealed interface UnknownExtension extends Extension {
  record UnknownControlExtension(byte label, DataBlock data) implements UnknownExtension, BlockType.ControlBlock {
    public UnknownControlExtension(byte label, DataBlock data) {
      this.label = label;
      this.data  = Objects.requireNonNull(data);
    }
  }

  record UnknownGraphicRenderingExtension(byte label, DataBlock data) implements UnknownExtension, BlockType.GraphicRenderingBlock {
    public UnknownGraphicRenderingExtension(byte label, DataBlock data) {
      this.label = label;
      this.data  = Objects.requireNonNull(data);
    }
  }

  record UnknownSpecialPurposeExtension(byte label, DataBlock data) implements UnknownExtension, BlockType.SpecialPurposeBlock {
    public UnknownSpecialPurposeExtension(byte label, DataBlock data) {
      this.label = label;
      this.data  = Objects.requireNonNull(data);
    }
  }
}
