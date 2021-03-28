package io.github.grantchan.sshengine.client;

import io.github.grantchan.sshengine.arch.SshMessage;
import io.github.grantchan.sshengine.client.connection.ClientChannel;
import io.github.grantchan.sshengine.client.connection.ExecChannel;
import io.github.grantchan.sshengine.client.connection.ShellChannel;
import io.github.grantchan.sshengine.common.AbstractSession;
import io.github.grantchan.sshengine.common.transport.compression.Compression;
import io.github.grantchan.sshengine.util.buffer.ByteBufIo;
import io.github.grantchan.sshengine.util.buffer.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import java.io.IOException;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.concurrent.CompletableFuture;

public class ClientSession extends AbstractSession {

  public ClientSession(Channel channel) {
    super(channel);
  }

  @Override
  public Cipher getInCipher() {
    return getS2cCipher();
  }

  @Override
  public void setInCipher(Cipher inCipher) {
    setS2cCipher(inCipher);
  }

  @Override
  public Cipher getOutCipher() {
    return getC2sCipher();
  }

  @Override
  public void setOutCipher(Cipher outCipher) {
    setC2sCipher(outCipher);
  }

  @Override
  public int getInCipherBlkSize() {
    return getS2cCipherBlkSize();
  }

  @Override
  public void setInCipherBlkSize(int inCipherBlkSize) {
    setS2cCipherBlkSize(inCipherBlkSize);
  }

  @Override
  public int getOutCipherBlkSize() {
    return getC2sCipherBlkSize();
  }

  @Override
  public void setOutCipherBlkSize(int outCipherBlkSize) {
    setC2sCipherBlkSize(outCipherBlkSize);
  }

  @Override
  public Mac getInMac() {
    return getS2cMac();
  }

  @Override
  public void setInMac(Mac inMac) {
    setS2cMac(inMac);
  }

  @Override
  public Mac getOutMac() {
    return getC2sMac();
  }

  @Override
  public void setOutMac(Mac outMac) {
    setC2sMac(outMac);
  }

  @Override
  public int getInMacSize() {
    return getS2cMacSize();
  }

  @Override
  public void setInMacSize(int inMacSize) {
    setS2cMacSize(inMacSize);
  }

  @Override
  public int getOutMacSize() {
    return getC2sMacSize();
  }

  @Override
  public void setOutMacSize(int outMacSize) {
    setC2sMacSize(outMacSize);
  }

  @Override
  public void setInDefMacSize(int inDefMacSize) {
    setS2cDefMacSize(inDefMacSize);
  }

  @Override
  public int getOutDefMacSize() {
    return getC2sDefMacSize();
  }

  @Override
  public void setOutDefMacSize(int outDefMacSize) {
    setC2sDefMacSize(outDefMacSize);
  }

  @Override
  public Compression getInCompression() {
    return getS2cCompression();
  }

  @Override
  public void setInCompression(Compression inCompression) {
    setS2cCompression(inCompression);
  }

  @Override
  public Compression getOutCompression() {
    return getC2sCompression();
  }

  @Override
  public void setOutCompression(Compression outCompression) {
    setC2sCompression(outCompression);
  }

  /**
   * Sends the {@link SshMessage#SSH_MSG_KEXDH_INIT} message to the server
   * @param e the public key generated by client, e = g ^ x mod p, where x is the client's private
   *          key.
   */
  public void requestKexDhInit(BigInteger e) {
    ByteBuf req = createMessage(SshMessage.SSH_MSG_KEXDH_INIT);

    ByteBufIo.writeMpInt(req, e);

    logger.debug("{} Requesting SSH_MSG_KEXDH_INIT...", this);

    channel.writeAndFlush(req);
  }

  @Override
  public void replyAccept(String svcName) {
    throw new UnsupportedOperationException("Client is not capable of replying accept message");
  }

  public CompletableFuture<Boolean> auth(String username, String password) {
    setUsername(username);

    ByteBuf req = createMessage(SshMessage.SSH_MSG_SERVICE_REQUEST);
    ByteBufIo.writeUtf8(req, "ssh-userauth");

    logger.debug("{} Requesting SSH_MSG_SERVICE_REQUEST...", this);

    channel.writeAndFlush(req).addListener(f -> {
      Throwable e = f.cause();
      if (e != null) {
        authFuture.completeExceptionally(e);
      } else if (f.isCancelled()) {
        authFuture.cancel(true);
      }
    });

    return authFuture;
  }

  public void requestUserAuthRequest(String user, String service, String method) {
    ByteBuf req = createMessage(SshMessage.SSH_MSG_USERAUTH_REQUEST);

    ByteBufIo.writeUtf8(req, user);
    ByteBufIo.writeUtf8(req, service);
    ByteBufIo.writeUtf8(req, method);

    logger.debug("{} Requesting SSH_MSG_USERAUTH_REQUEST... username:{}, service:{}, method:{}",
        this, user, service, method);

    channel.writeAndFlush(req);
  }

  public void requestUserAuthRequest(String user, String service, String method, String algo,
                                     PublicKey pubKey) throws IOException {
    ByteBuf req = createMessage(SshMessage.SSH_MSG_USERAUTH_REQUEST);

    ByteBufIo.writeUtf8(req, user);
    ByteBufIo.writeUtf8(req, service);
    ByteBufIo.writeUtf8(req, method);
    req.writeBoolean(false);
    ByteBufIo.writeUtf8(req, algo);
    ByteBufIo.writePublicKey(req, pubKey);

    logger.debug("{} Requesting SSH_MSG_USERAUTH_REQUEST... " +
        "username:{}, service:{}, method:{}, algo:{}", this, user, service, method, algo);

    channel.writeAndFlush(req);
  }

  public void requestUserAuthRequest(String user, String service, String method, String algo,
                                     PublicKey pubKey, byte[] sig) throws IOException {
    ByteBuf req = createMessage(SshMessage.SSH_MSG_USERAUTH_REQUEST);

    ByteBufIo.writeUtf8(req, user);
    ByteBufIo.writeUtf8(req, service);
    ByteBufIo.writeUtf8(req, method);
    req.writeBoolean(true);
    ByteBufIo.writeUtf8(req, algo);
    ByteBufIo.writePublicKey(req, pubKey);
    ByteBufIo.writeBytes(req, sig);

    logger.debug("{} Requesting SSH_MSG_USERAUTH_REQUEST... " +
        "username:{}, service:{}, method:{}, algo:{}, sigature: {}", this, user, service,
        method, algo, Bytes.md5(sig));

    channel.writeAndFlush(req);
  }

  /**
   * Opening a Channel
   *
   * When either side wishes to open a new channel, it allocates a local
   * number for the channel.  It then sends the following message to the
   * other side, and includes the local channel number and initial window
   * size in the message.
   *
   *    byte      SSH_MSG_CHANNEL_OPEN
   *    string    channel type in US-ASCII only
   *    uint32    sender channel
   *    uint32    initial window size
   *    uint32    maximum packet size
   *    ....      channel type specific data follows
   *
   * The 'channel type' is a name, as described in [SSH-ARCH] and
   * [SSH-NUMBERS], with similar extension mechanisms.  The 'sender
   * channel' is a local identifier for the channel used by the sender of
   * this message.  The 'initial window size' specifies how many bytes of
   * channel data can be sent to the sender of this message without
   * adjusting the window.  The 'maximum packet size' specifies the
   * maximum size of an individual data packet that can be sent to the
   * sender.  For example, one might want to use smaller packets for
   * interactive connections to get better interactive response on slow
   * links.
   *
   * The remote side then decides whether it can open the channel, and
   * responds with either SSH_MSG_CHANNEL_OPEN_CONFIRMATION or
   * SSH_MSG_CHANNEL_OPEN_FAILURE.
   */
  public ChannelFuture sendChannelOpen(String type, int id, int wndSize, int pkgSize) {
    ByteBuf co = createMessage(SshMessage.SSH_MSG_CHANNEL_OPEN);

    ByteBufIo.writeUtf8(co, type);
    co.writeInt(id);
    co.writeInt(wndSize);
    co.writeInt(pkgSize);

    logger.debug("{} Requesting SSH_MSG_CHANNEL_OPEN..." +
        "type:{}, id:{}, window size:{}, package size:{}", this, type, id, wndSize, pkgSize);

    return channel.writeAndFlush(co);
  }

  public void sendChannelShell(int recipient) {
    checkActive("sendChannelShell");

    ByteBuf cs = createMessage(SshMessage.SSH_MSG_CHANNEL_REQUEST);

    cs.writeInt(recipient);
    ByteBufIo.writeUtf8(cs, "shell");
    cs.writeBoolean(false); // want reply

    logger.debug("{} Sending SSH_MSG_CHANNEL_REQUEST... recipient: {}, type: shell," +
        " want-reply: false", this, recipient);

    channel.writeAndFlush(cs);
  }

  public void sendChannelExec(int recipient, String command) {
    checkActive("sendChannelExec");

    ByteBuf ce = createMessage(SshMessage.SSH_MSG_CHANNEL_REQUEST);

    ce.writeInt(recipient);
    ByteBufIo.writeUtf8(ce, "exec");
    ce.writeBoolean(false); // want reply

    logger.debug("{} Sending SSH_MSG_CHANNEL_REQUEST... recipient: {}, type: exec," +
        " want-reply: false", this, recipient);

    channel.writeAndFlush(ce);
  }

  public ClientChannel createChannel(String type, String... args) {
    switch(type) {
      case "shell":
        return new ShellChannel(this);

      case "exec":
        return new ExecChannel(this, String.join(" ", args));
    }

    return null;
  }
}
