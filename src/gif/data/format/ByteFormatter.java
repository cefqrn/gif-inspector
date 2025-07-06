package gif.data.format;

public class ByteFormatter {
  public static String format(byte x) {
    return String.format("0x%02x", x);
  }
}
