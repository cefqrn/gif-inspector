package serializable;

import java.io.IOException;
import java.io.OutputStream;

public interface Serializable {
  void writeTo(OutputStream stream) throws IOException;
}
