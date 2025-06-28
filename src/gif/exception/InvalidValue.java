package gif.exception;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InvalidValue extends ParseException {
  public InvalidValue(String message) {
    super(message);
  }

  @SafeVarargs
  public <T> InvalidValue(Function<T, String> format, String name, T got, T... expected) {
    super(String.format(
      expected.length == 1
        ? "Invalid value for %s: got %s, expected %s"
        : "Invalid value for %s: got %s, expected one of %s",
      name,
      format.apply(got),
      Arrays.stream(expected).map(format).collect(Collectors.joining(", "))
    ));
  }

  @SafeVarargs
  public <T extends Object> InvalidValue(String name, T got, T... expected) {
    this(T::toString, name, got, expected);
  }

  public static String formatByte(int x) {
    return String.format("0x%02x", x);
  }

  public static String formatByteArray(int groupSize, byte[] data) {
    var output = "";
    var left = data.length;
    for (int i=0; i < data.length; i += groupSize, left -= groupSize) {
      for (int j=0; j < Math.min(groupSize, left); ++j)
        output += String.format("%02x", data[i+j]);

      if (left > groupSize)
        output += " ";
    }

    return output;
  }

  public static String formatByteArray(byte[] a) {
    return formatByteArray(4, a);
  }
}
