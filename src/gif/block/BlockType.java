package gif.block;

public sealed interface BlockType {
  non-sealed interface ControlBlock          extends BlockType {}
  non-sealed interface GraphicRenderingBlock extends BlockType {}
  non-sealed interface SpecialPurposeBlock   extends BlockType {}

  public static boolean isLabelForGraphicRenderingBlock(byte label) {
    var value = Byte.toUnsignedInt(label);
    return 0x00 <= value && value <= 0x7f;
  }

  public static boolean isLabelForControlBlock(byte label) {
    var value = Byte.toUnsignedInt(label);
    return 0x80 <= value && value <= 0xf9;
  }

  public static boolean isLabelForSpecialPurposeBlock(byte label) {
    var value = Byte.toUnsignedInt(label);
    return 0xfa <= value && value <= 0xff;
  }
}
