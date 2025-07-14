package gif.data.exception;

public class ParseException extends IllegalArgumentException {
  public ParseException() {}

  public ParseException(String message) {
    super(message);
  }
}
