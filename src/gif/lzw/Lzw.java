package gif.lzw;

import java.util.ArrayList;
import java.util.Arrays;

import gif.exception.OutOfBounds;
import gif.exception.ParseException;

public class Lzw {
  public static final int MAXIMUM_CODE_SIZE = 12;

  public static ArrayList<Integer> decode(BitStream stream, int minimumCodeSize) throws ParseException {
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
        break;

      if (code == Code.CLEAR) {
        codes = getInitialCodes(minimumCodeSize);
        codeSize = minimumCodeSize + 1;

        previousPattern = null;

        continue;
      }

      var pattern = (Code.Pattern)code;
      for (var value : pattern.getValues())
        result.add(value);

      if (reachedLastPattern)
        continue;

      if (previousPattern != null) {
        codes.add(previousPattern.with(pattern.getInitialValue()));
        if (isLastPatternForCurrentSize)
          codeSize++;
      }

      previousPattern = pattern;
    }

    return result;
  }

  private static ArrayList<Code> getInitialCodes(int minimumCodeSize) {
    var codes = new ArrayList<Code>((1 << minimumCodeSize) + 2);
    for (var i=0; i < (1 << minimumCodeSize); ++i)
      codes.add(new Code.Pattern(new int[]{i}));

    codes.add(Code.CLEAR);
    codes.add(Code.END_OF_INFORMATION);

    return codes;
  }

  private static class Code {
    public static final ClearCode CLEAR = new ClearCode();
    public static final EndOfInformationCode END_OF_INFORMATION = new EndOfInformationCode();

    private static class ClearCode extends Code {}
    private static class EndOfInformationCode extends Code {}

    public static class Pattern extends Code {
      private final int values[];

      public Pattern(int values[]) {
        this.values = values.clone();
      }

      public int[] getValues() {
        return values.clone();
      }

      public int getInitialValue() {
        return values[0];
      }

      public Pattern with(int value) {
        var newValues = Arrays.copyOf(values, values.length + 1);
        newValues[values.length] = value;

        return new Pattern(newValues);
      }

      public Pattern extended() {
        return this.with(getInitialValue());
      }
    }
  }
}
