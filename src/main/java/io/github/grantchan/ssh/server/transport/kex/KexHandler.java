package io.github.grantchan.ssh.server.transport.kex;

import io.github.grantchan.ssh.common.Session;
import io.github.grantchan.ssh.common.transport.cipher.CipherFactories;
import io.github.grantchan.ssh.common.transport.kex.KexInitParam;
import io.github.grantchan.ssh.common.transport.kex.KeyExchange;
import io.github.grantchan.ssh.common.transport.mac.MacFactories;
import io.github.grantchan.ssh.util.buffer.SshByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;
import java.util.Objects;

public abstract class KexHandler {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  protected MessageDigest md;
  protected KeyExchange kex;
  protected Session session;

  protected byte[] h = null;

  public KexHandler(MessageDigest md, KeyExchange kex, Session session) {
    this.md = md;
    this.kex = kex;
    this.session = session;
  }

  public abstract void handleMessage(int cmd, ByteBuf req) throws IOException;

  public void handleNewKeys(ByteBuf msg) {
    byte[] id = h;
    StringBuilder sb = new StringBuilder();
    for (byte b : id) {
      sb.append("0123456789abcdef".charAt((b >> 4) & 0x0F));
      sb.append("0123456789abcdef".charAt(b & 0x0F));
      sb.append(":");
    }
    logger.info("SSH_MSG_NEWKEYS: {}", sb.toString());

    session.setId(id);

    ByteBuf buf = session.createBuffer();

    byte[] k = kex.getSecretKey();
    SshByteBuf.writeMpInt(buf, k);
    buf.writeBytes(id);
    buf.writeByte((byte) 0x41);
    buf.writeBytes(id);

    int readableBytes = buf.readableBytes();
    byte[] array = new byte[readableBytes];
    buf.readBytes(array);

    int j = readableBytes - id.length - 1;

    md.update(array);
    byte[] iv_c2s = md.digest();

    array[j]++;
    md.update(array);
    byte[] iv_s2c = md.digest();

    array[j]++;
    md.update(array);
    byte[] e_c2s = md.digest();

    array[j]++;
    md.update(array);
    byte[] e_s2c = md.digest();

    array[j]++;
    md.update(array);
    byte[] mac_c2s = md.digest();

    array[j]++;
    md.update(array);
    byte[] mac_s2c = md.digest();

    List<String> kp = session.getKexInit();

    // server to client cipher
    CipherFactories cf;
    cf = Objects.requireNonNull(CipherFactories.from(kp.get(KexInitParam.ENCRYPTION_S2C)));
    e_s2c = hashKey(e_s2c, cf.getBlkSize(), k);
    Cipher s2cCip = Objects.requireNonNull(cf.create(e_s2c, iv_s2c, Cipher.ENCRYPT_MODE));

    session.setS2cCipher(s2cCip);
    session.setS2cCipherSize(cf.getIvSize());

    // client to server cipher
    cf = Objects.requireNonNull(CipherFactories.from(kp.get(KexInitParam.ENCRYPTION_C2S)));
    e_c2s = hashKey(e_c2s, cf.getBlkSize(), k);
    Cipher c2sCip = Objects.requireNonNull(cf.create(e_c2s, iv_c2s, Cipher.DECRYPT_MODE));

    session.setC2sCipher(c2sCip);
    session.setC2sCipherSize(cf.getIvSize());

    // server to client MAC
    MacFactories mf;
    mf = Objects.requireNonNull(MacFactories.from(kp.get(KexInitParam.MAC_S2C)));
    Mac s2cMac = Objects.requireNonNull(mf.create(mac_s2c));

    session.setS2cMac(s2cMac);
    session.setS2cMacSize(mf.getBlkSize());
    session.setS2cDefMacSize(mf.getDefBlkSize());

    // client to server MAC
    mf = Objects.requireNonNull(MacFactories.from(kp.get(KexInitParam.MAC_C2S)));
    Mac c2sMac = Objects.requireNonNull(mf.create(mac_c2s));

    session.setC2sMac(c2sMac);
    session.setC2sMacSize(mf.getBlkSize());
    session.setC2sDefMacSize(mf.getDefBlkSize());
  }

  private byte[] hashKey(byte[] e, int blockSize, byte[] k) {
    for (ByteBuf b = Unpooled.buffer(); e.length < blockSize; b.clear()) {
      SshByteBuf.writeMpInt(b, k);
      b.writeBytes(h);
      b.writeBytes(e);
      byte[] a = new byte[b.readableBytes()];
      b.readBytes(a);
      md.update(a);

      byte[] foo = md.digest();
      byte[] bar = new byte[e.length + foo.length];
      System.arraycopy(e, 0, bar, 0, e.length);
      System.arraycopy(foo, 0, bar, e.length, foo.length);
      e = bar;
    }
    return e;
  }
}