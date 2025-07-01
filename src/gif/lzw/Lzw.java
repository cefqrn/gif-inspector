package gif.lzw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gif.data.exception.OutOfBounds;
import gif.data.exception.ParseException;

public class Lzw {
  public static final int MAXIMUM_CODE_SIZE = 12;

  public static List<Integer> decode(BitStream stream, int minimumCodeSize) throws ParseException {
    var codes = getInitialCodes(minimumCodeSize);
    var codeSize = minimumCodeSize + 1;

    Code.Pattern previousPattern = null;

    var result = new ArrayList<Integer>();

    while (true) {
      var codeIndex = stream.read(codeSize);

      var isLastPatternForCurrentSize = codes.size() == (1 << codeSize) - 1;
      var reachedLastPattern = isLastPatternForCurrentSize && codeSize == MAXIMUM_CODE_SIZE;

      Code code = null;
      if (codeIndex < codes.size()) {
        code = codes.get(codeIndex);
      } else if (codeIndex == codes.size() && !reachedLastPattern && previousPattern != null) {
        code = previousPattern.extended();
      } else {
        throw new OutOfBounds("LZW code table index", codeIndex, 0, codes.size() - 1);
      }

      if (code == Code.END_OF_INFORMATION)
        return result;

      if (code == Code.CLEAR) {
        codes = getInitialCodes(minimumCodeSize);
        codeSize = minimumCodeSize + 1;

        previousPattern = null;

        continue;
      }

      var pattern = (Code.Pattern)code;

      result.addAll(pattern.values);

      if (reachedLastPattern)
        continue;

      if (previousPattern != null) {
        codes.add(previousPattern.with(pattern.values.getFirst()));
        if (isLastPatternForCurrentSize)
          codeSize++;
      }

      previousPattern = pattern;
    }
  }

  private static List<Code> getInitialCodes(int minimumCodeSize) {
    var codes = new ArrayList<Code>((1 << minimumCodeSize) + 2);
    for (var i=0; i < (1 << minimumCodeSize); ++i)
      codes.add(new Code.Pattern(List.of(i)));

    codes.add(Code.CLEAR);
    codes.add(Code.END_OF_INFORMATION);

    return codes;
  }

  private static sealed interface Code {
    static final ClearCode CLEAR = new ClearCode();
    static final EndOfInformationCode END_OF_INFORMATION = new EndOfInformationCode();

    record ClearCode()                   implements Code {}
    record EndOfInformationCode()        implements Code {}

    record Pattern(List<Integer> values) implements Code {
      public Pattern with(int value) {
        var newValues = new ArrayList<>(values);
        newValues.add(value);

        return new Pattern(Collections.unmodifiableList(newValues));
      }

      public Pattern extended() {
        return this.with(values.getFirst());
      }
    }
  }
}
