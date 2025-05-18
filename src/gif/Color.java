package gif;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import exceptions.ParseException;
import serializable.LittleEndian;
import serializable.Serializable;

public class Color implements Serializable {
  public int red;
  public int green;
  public int blue;
  public boolean isTransparent;

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    LittleEndian.writeU8To(stream, red);
    LittleEndian.writeU8To(stream, green);
    LittleEndian.writeU8To(stream, blue);
  }

  @Override
  public void readFrom(InputStream stream) throws IOException, ParseException {
    red   = LittleEndian.readU8From(stream);
    green = LittleEndian.readU8From(stream);
    blue  = LittleEndian.readU8From(stream);
  }
}
