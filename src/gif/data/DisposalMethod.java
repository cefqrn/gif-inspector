package gif.data;

import gif.data.exception.OutOfBounds;

public enum DisposalMethod {
  UNSPECIFIED,
  DO_NOT_DISPOSE,
  RESTORE_BACKGROUND,
  RESTORE_PREVIOUS,
  METHOD4,  // undefined
  METHOD5,
  METHOD6,
  METHOD7;

  public static DisposalMethod from(int encodedValue) throws OutOfBounds {
    return switch (encodedValue) {
      case 0 -> UNSPECIFIED;
      case 1 -> DO_NOT_DISPOSE;
      case 2 -> RESTORE_BACKGROUND;
      case 3 -> RESTORE_PREVIOUS;
      case 4 -> METHOD4;
      case 5 -> METHOD5;
      case 6 -> METHOD6;
      case 7 -> METHOD7;
      default ->
        throw new OutOfBounds("disposal method", encodedValue, 0, 7);
    };
  }

  public int encodedValue() {
    return switch (this) {
      case UNSPECIFIED        -> 0;
      case DO_NOT_DISPOSE     -> 1;
      case RESTORE_BACKGROUND -> 2;
      case RESTORE_PREVIOUS   -> 3;
      case METHOD4            -> 4;
      case METHOD5            -> 5;
      case METHOD6            -> 6;
      case METHOD7            -> 7;
    };
  }
}
