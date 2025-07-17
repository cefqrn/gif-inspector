package gif.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gif.data.exception.OutOfBounds;
import gif.data.exception.UnexpectedEndOfStream;

public sealed abstract class Unsigned extends Number implements Comparable<Unsigned>, Serializable {
  protected final int value;

  private Unsigned(String name, int value, int bound) {
    this.value = OutOfBounds.check(name, value, 0, bound);
  }

  @Override public int    intValue()    { return         value; }
  @Override public long   longValue()   { return (long  )value; }
  @Override public float  floatValue()  { return (float )value; }
  @Override public double doubleValue() { return (double)value; }

  @Override
  public boolean equals(Object o) {
    return o instanceof Unsigned oUnsigned && this.value == oUnsigned.value;
  }

  @Override
  public int compareTo(Unsigned o) {
    return Integer.valueOf(this.value).compareTo(Integer.valueOf(o.value));
  }

  @Override
  public String toString() {
    return String.format("%du", value);
  }

  public static final class Byte extends Unsigned {
    public static final Byte ZERO = new Byte(0);

    public Byte(int value) throws OutOfBounds {
      super("unsigned byte", value, 0xff);
    }

    public static Byte readFrom(InputStream stream) throws IOException, UnexpectedEndOfStream {
      var a = stream.read();
      if (a < 0)
        throw new UnexpectedEndOfStream();

      return new Byte(a);
    }

    public static List<Byte> readListFrom(InputStream stream, int length) throws IOException, UnexpectedEndOfStream {
      if (length < 0)
        throw new IllegalArgumentException("length must be nonnegative (got " + length + ")");

      var result = new ArrayList<Byte>(length);
      for (var i=0; i < length; ++i)
        result.add(Byte.readFrom(stream));

      return Collections.unmodifiableList(result);
    }

    @Override
    public void writeTo(OutputStream stream) throws IOException {
      stream.write(value);
    }
  }

  public static final class Short extends Unsigned {
    public static final Short ZERO = new Short(0);

    public Short(int value) throws OutOfBounds {
      super("unsigned short", value, 0xffff);
    }

    public static Short readFrom(InputStream stream) throws IOException, UnexpectedEndOfStream {
      var a = stream.read();
      var b = stream.read();
      if (b < 0)
        throw new UnexpectedEndOfStream();

      return new Short((b << 8) | a);
    }

    public static Short fromBytes(Byte lo, Byte hi) {
      return new Short((hi.intValue() << 8) | lo.intValue());
    }

    @Override
    public void writeTo(OutputStream stream) throws IOException {
      stream.write(value);
      stream.write(value >> 8);
    }
  }
}
