package gif.lzw;

import java.util.ArrayList;
import java.util.Iterator;

import gif.data.DataBlock;
import gif.exception.UnexpectedEndOfStream;

public class BitStream {
  private final Iterator<Byte> data;
  private int currentByte = 0;
  private int bitsLeftInCurrentByte = 0;

  public BitStream(DataBlock dataBlock) {
    var bytes = new ArrayList<Byte>(dataBlock.totalSize());
    for (var subBlock : dataBlock.subBlocks())
      for (var b : subBlock.data)
        bytes.add(b);

    data = bytes.iterator();
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

      currentByte = data.next();
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
