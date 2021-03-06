package io.github.grantchan.sshengine.util.publickey;

import io.github.grantchan.sshengine.common.transport.kex.ECurve;
import io.github.grantchan.sshengine.util.buffer.Bytes;
import sun.security.provider.DSAPublicKey;

import java.security.PublicKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECParameterSpec;
import java.util.Objects;

public final class PublicKeyUtil {

  /**
   * Compare two public keys
   *
   * <p>by calling the overload method where the certain key object belongs to, it could be:
   * <ul><li>{@link DSAPublicKey} or</li>
   *     <li>{@link RSAPublicKey} or</li>
   *     <li>{@link ECPublicKey}</li></ul></p>
   *
   * @param a  one of public keys to compare
   * @param b  another public key to compare
   * @return   {@code true} if they're identical, {@code false} if they're not identical, or they're
   *           not belong to same certain {@link PublicKey} class
   *
   * @see #compare(DSAParams, DSAParams)
   * @see #compare(RSAPublicKey, RSAPublicKey)
   * @see #compare(ECPublicKey, ECPublicKey)
   */
  public static boolean compare(PublicKey a, PublicKey b) {
    if ((a instanceof DSAPublicKey) && (b instanceof DSAPublicKey)) {
      return compare((DSAPublicKey) a, (DSAPublicKey) b);
    } else if ((a instanceof RSAPublicKey) && (b instanceof RSAPublicKey)) {
      return compare((RSAPublicKey) a, (RSAPublicKey) b);
    } else if ((a instanceof ECPublicKey) && (b instanceof ECPublicKey)) {
      return compare((ECPublicKey) a, (ECPublicKey) b);
    }
    return false;
  }

  /**
   * Compare two DSA public keys
   * <p>by comparing:
   * <ul><li>the public key values via {@link DSAPublicKey#getY()}</li>
   *     <li>the DSA-specific key parameters via {@link DSAPublicKey#getParams()}</li></ul>
   * of two keys</p>
   *
   * @param a  one of {@link DSAPublicKey} to compare
   * @param b  another {@link DSAPublicKey}
   * @return   {@code true} if they're identical, otherwise, {@code false}
   *
   * @see #compare(DSAParams, DSAParams)
   */
  private static boolean compare(DSAPublicKey a, DSAPublicKey b) {
    if (a == b) { // if both are null
      return true;
    }
    if (a == null || b == null) { // if either is null
      return false;
    }
    // if all of them are not null
    return Objects.equals(a.getY(), b.getY())
        && compare(a.getParams(), b.getParams());
  }

  /**
   * Compare two DSAParam objects
   * <p>by comparing:
   * <ul><li>the prime, P via {@link DSAParams#getP()}</li>
   *     <li>the base, G via {@link DSAParams#getG()}</li>
   *     <li>the subprime, Q via {@link DSAParams#getQ()}</li></ul>
   * of two keys</p>
   *
   * @param a  one of {@link DSAParams} to compare
   * @param b  another {@link DSAParams} to compare
   * @return   {@code true} if they're identical, otherwise, {@code false}
   */
  private static boolean compare(DSAParams a, DSAParams b) {
    if (a == b) { // if both are null
      return true;
    }
    if (a == null || b == null) { // if either is null
      return false;
    }
    // if all of them are not null
    return Objects.equals(a.getP(), b.getP())
        && Objects.equals(a.getG(), b.getG())
        && Objects.equals(a.getQ(), b.getQ());
  }

  /**
   * Compare two RSA public keys
   * <p>by comparing:
   * <ul><li>the public exponent values via {@link RSAPublicKey#getPublicExponent()}</li>
   *     <li>the modulus via {@link RSAPublicKey#getModulus()}</li></ul>
   * of two keys</p>
   *
   * @param a  one of {@link RSAPublicKey} to compare
   * @param b  another {@link RSAPublicKey}
   * @return   {@code true} if they're identical, otherwise, {@code false}
   */
  private static boolean compare(RSAPublicKey a, RSAPublicKey b) {
    if (a == b) { // if both are null
      return true;
    }
    if (a == null || b == null) { // if either is null
      return false;
    }
    // if all of them are not null
    return Objects.equals(a.getPublicExponent(), b.getPublicExponent())
        && Objects.equals(a.getModulus(), b.getModulus());
  }

  /**
   * Compare two EC(Elliptic Curve) public keys
   * <p>by comparing:
   * <ul><li>the public point W via {@link ECPublicKey#getW()}</li>
   *     <li>domain parameters via {@link ECPublicKey#getParams()}</li></ul>
   * of two keys</p>
   *
   * @param a  one of {@link ECPublicKey} to compare
   * @param b  another {@link ECPublicKey}
   * @return   {@code true} if they're identical, otherwise, {@code false}
   *
   * @see #compare(ECParameterSpec, ECParameterSpec)
   */
  private static boolean compare(ECPublicKey a, ECPublicKey b) {
    if (a == b) { // if both are null
      return true;
    }
    if (a == null || b == null) { // if either is null
      return false;
    }
    // if all of them are not null
    return Objects.equals(a.getW(), b.getW())
        && compare(a.getParams(), b.getParams());
  }

  /**
   * Compare two {@link ECParameterSpec} object.
   * <p>by comparing:
   * <ul><li>the cofactor via {@link ECParameterSpec#getCofactor()}</li>
   *     <li>the elliptic curve via {@link ECParameterSpec#getCurve()}</li>
   *     <li>the generator via {@link ECParameterSpec#getGenerator()}</li>
   *     <li>the order of generator via {@link ECParameterSpec#getOrder()}</li></ul>
   * of two keys</p>
   *
   * @param a one of {@link ECParameterSpec} to compare
   * @param b another {@link ECParameterSpec}
   * @return  {@code true} if they're identical, otherwise, {@code false}
   */
  private static boolean compare(ECParameterSpec a, ECParameterSpec b) {
    if (a == b) { // if both are null
      return true;
    }
    if (a == null || b == null) { // if either is null
      return false;
    }
    // if all of them are not null
    return Objects.equals(a.getCofactor(), b.getCofactor())
        && Objects.equals(a.getCurve(), b.getCurve())
        && Objects.equals(a.getGenerator(), b.getGenerator())
        && Objects.equals(a.getOrder(), b.getOrder());
  }

  /**
   * Return the type of a {@link PublicKey}
   * <p>By far, it supports {@link DSAPublicKey}, {@link RSAPublicKey}, {@link ECPublicKey}</p>
   *
   * @param pubKey  Input {@link PublicKey}
   * @return        The string of the type of the given {@code pubKey}
   */
  public static String typeOf(PublicKey pubKey) {
    Objects.requireNonNull(pubKey, "Invalid parameter - pubKey is null");

    if (pubKey instanceof DSAPublicKey) {
      return "ssh-dss";
    } else if (pubKey instanceof RSAPublicKey) {
      return "ssh-rsa";
    } else if (pubKey instanceof ECPublicKey) {
      ECPublicKey ecPubKey = (ECPublicKey) pubKey;
      ECurve curve = ECurve.from(ecPubKey.getParams());
      if (curve != null) {
        return "ecdsa-sha2-" + curve.name();
      }
    }
    return null;
  }

  /**
   * Return the bytes of a {@link PublicKey}
   * <p>by calling the overload method where the certain key object belongs to, it could be:
   * <ul><li>{@link DSAPublicKey} or</li>
   *     <li>{@link RSAPublicKey} or</li>
   *     <li>{@link ECPublicKey}</li></ul></p>
   *
   * @param pubKey  The {@link PublicKey} to convert to byte array
   * @return        Byte arrays converted from {@code pubKey} if successful, otherwise, return null.
   *
   * @see #bytesOf(DSAPublicKey)
   * @see #bytesOf(RSAPublicKey)
   * @see #bytesOf(ECPublicKey)
   */
  public static byte[] bytesOf(PublicKey pubKey) {
    Objects.requireNonNull(pubKey, "Invalid parameter - pubKey is null");

    if (pubKey instanceof DSAPublicKey) {
      return bytesOf((DSAPublicKey) pubKey);
    } else if (pubKey instanceof RSAPublicKey) {
      return bytesOf((RSAPublicKey) pubKey);
    } else if (pubKey instanceof ECPublicKey) {
      return bytesOf((ECPublicKey) pubKey);
    }
    return null;
  }

  /**
   * Return the bytes of a {@link DSAPublicKey}
   * <p>by combining:
   * <ul><li>the DSA key type string, "ssh-dsa"</li>
   *     <li>the prime, P via {@link DSAParams#getP()}</li>
   *     <li>the subprime, Q via {@link DSAParams#getQ()}</li>
   *     <li>the base, G via {@link DSAParams#getG()}</li>
   *     <li>the public key value, Y via {@link DSAPublicKey#getY()}</li></ul></p>
   *
   * @param pubKey The {@link DSAPublicKey} to convert to byte array
   * @return       Byte arrays converted from {@code pubKey} if successful, otherwise, return null.
   */
  private static byte[] bytesOf(DSAPublicKey pubKey) {
    Objects.requireNonNull(pubKey, "Invalid parameter - pubKey is null");

    DSAParams params = pubKey.getParams();
    return Bytes.concat(
        Bytes.addLen("ssh-dsa"),
        Bytes.joinWithLength(params.getP(), params.getQ(), params.getG(), pubKey.getY())
    );
  }

  /**
   * Return the bytes of a {@link RSAPublicKey}
   * <p>by combining:
   * <ul><li>the RSA key type string, "ssh-rsa"</li>
   *     <li>the public exponent values via {@link RSAPublicKey#getPublicExponent()}</li>
   *     <li>the modulus via {@link RSAPublicKey#getModulus()}</li></ul></p>
   *
   * @param pubKey The {@link RSAPublicKey} to convert to byte array
   * @return       Byte arrays converted from {@code pubKey} if successful, otherwise, return null.
   */
  private static byte[] bytesOf(RSAPublicKey pubKey) {
    Objects.requireNonNull(pubKey, "Invalid parameter - pubKey is null");

    return Bytes.concat(
        Bytes.addLen("ssh-rsa"),
        Bytes.joinWithLength(pubKey.getPublicExponent(), pubKey.getModulus())
    );
  }

  /**
   * Return the bytes of a {@link ECPublicKey}
   * <p>by combining:
   * <ul><li>the curve type name, in string, start with "ecdsa-sha2-"</li>
   *     <li>the curve name, in string</li>
   *     <li>the public key, via:
   *     <ul><li>the public point W via {@link ECPublicKey#getW()}</li>
   *         <li>the domain parameters via {@link ECPublicKey#getParams()}</li></ul></li>
   * </ul></p>
   *
   * @param pubKey The {@link ECPublicKey} to convert to byte array
   * @return       Byte arrays converted from {@code pubKey} if successful, otherwise, return null.
   */
  private static byte[] bytesOf(ECPublicKey pubKey) {
    Objects.requireNonNull(pubKey, "Invalid parameter - pubKey is null");

    ECParameterSpec params = pubKey.getParams();
    ECurve curve = ECurve.from(params);
    return curve == null ? null : Bytes.concat(
            Bytes.joinWithLength("ecdsa-sha2-" + curve.name(), curve.name()),
            Bytes.addLen(ECurve.bytesOf(pubKey.getW(), params.getCurve()))
        );
  }

  /* Private constructor to prevent this class from being explicitly instantiated */
  private PublicKeyUtil() {}
}
