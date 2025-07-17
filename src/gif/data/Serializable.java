package gif.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public interface Serializable {
  void writeTo(OutputStream stream) throws IOException;

  default void writeTo(ByteArrayOutputStream stream) {
    try {
      writeTo((OutputStream)stream);
    } catch (IOException e) {}  // impossible
  }
}
