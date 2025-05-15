package serializable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Serializable {
  public void writeTo(OutputStream stream) throws IOException;
  public void readFrom(InputStream stream) throws IOException, ParseException;
}
