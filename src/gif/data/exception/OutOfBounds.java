package gif.data.exception;

import java.util.function.Function;

public class OutOfBounds extends InvalidValue {
  public <T> OutOfBounds(Function<T, String> format, String name, T got, T lowerBound, T upperBound) {
    super(String.format(
      "Invalid value for %s: got %s, expected value between %s and %s",
      name,
      format.apply(got),
      format.apply(lowerBound),
      format.apply(upperBound)
    ));
  }

  public <T> OutOfBounds(String name, T got, T lowerBound, T upperBound) {
    this(T::toString, name, got, lowerBound, upperBound);
  }
}
