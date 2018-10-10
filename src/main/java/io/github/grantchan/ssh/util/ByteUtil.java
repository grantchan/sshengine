package io.github.grantchan.ssh.util;

public final class ByteUtil {

  public static byte[] resizeKey(byte[] array, int newSize) {
    if (array.length > newSize) {
      byte[] tmp = new byte[newSize];
      System.arraycopy(array, 0, tmp, 0, newSize);
      array = tmp;
    }
    return array;
  }

  /**
   * Convert the unsigned integer {@code i} from host byte order to network byte order
   *
   * @param i the unsigned integer in host byte order
   * @return the network byte order byte array of {@code i}
   */
  public static byte[] htonl(long i) {
    byte[] n = new byte[4];
    n[0] = (byte) (i >>> 24);
    n[1] = (byte) (i >>> 16);
    n[2] = (byte) (i >>> 8);
    n[3] = (byte) i;

    return n;
  }

  /* Private constructor to prevent this class from being explicitly instantiated */
  private ByteUtil() {}
}
