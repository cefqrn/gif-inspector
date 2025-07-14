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

  @SafeVarargs
  public static <T> T check(Function<T, String> format, String name, T value, T... expected) throws InvalidValue {
    for (var possibility : expected)
      if (value.equals(possibility))
        return value;

    throw new InvalidValue(format, name, value, expected);
  }

  @SafeVarargs
  public static <T> T check(String name, T value, T... expected) throws InvalidValue {
    return check(T::toString, name, value, expected);
  }
}
