package gif.data.exception;

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
}
