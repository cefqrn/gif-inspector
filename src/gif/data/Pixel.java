package gif.data;

import java.util.Objects;

public record Pixel(Color color, boolean isTransparent) {
  public Pixel(Color color, boolean isTransparent) {
    this.color = Objects.requireNonNull(color);
    this.isTransparent = isTransparent;
  }
}
