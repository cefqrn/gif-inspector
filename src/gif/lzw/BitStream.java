package gif.lzw;

import java.util.Iterator;
import java.util.List;

import gif.data.DataBlock;
import gif.data.Unsigned;
import gif.data.DataBlock.SubBlock;
import gif.data.exception.UnexpectedEndOfStream;

public class BitStream {
  private final Iterator<Unsigned.Byte> data;
  private int currentByte = 0;
  private int bitsLeftInCurrentByte = 0;

  public BitStream(DataBlock dataBlock) {
    data = dataBlock.subBlocks().stream()
      .map(SubBlock::data)
      .flatMap(List::stream)
      .iterator();
  }

  private static int reverseBits(int bits, int bitCount) {
    var output = 0;
    for (int i=0; i < bitCount; ++i) {
      output <<= 1;
      output |= bits & 1;
      bits >>= 1;
    }

    return output;
  }

  public int read() throws UnexpectedEndOfStream {
    if (bitsLeftInCurrentByte == 0) {
      if (!data.hasNext())
        throw new UnexpectedEndOfStream();

      currentByte = data.next().intValue();
      bitsLeftInCurrentByte = 8;
    }

    var result = currentByte & 1;

    currentByte >>= 1;
    bitsLeftInCurrentByte--;

    return result;
  }

  public int read(int bitCount) throws UnexpectedEndOfStream {
    int bits = 0;
    for (int i=0; i < bitCount; ++i) {
      bits <<= 1;
      bits |= read();
    }

    return BitStream.reverseBits(bits, bitCount);
  }
}
