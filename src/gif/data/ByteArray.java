package gif.data;

public class ByteArray {
  public static String format(int groupSize, byte[] data) {
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

  public static String format(byte[] a) {
    return ByteArray.format(4, a);
  }
}
