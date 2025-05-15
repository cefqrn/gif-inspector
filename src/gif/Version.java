package gif;

public enum Version {
  VERSION_87A("87a".getBytes()),
  VERSION_89A("89a".getBytes());

  private final byte[] data;

  private Version(byte[] data) {
    this.data = data.clone();
  }

  public byte[] getData() {
    return data.clone();
  }
}
