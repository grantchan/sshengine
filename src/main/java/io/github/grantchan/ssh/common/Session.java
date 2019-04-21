package io.github.grantchan.ssh.common;

import io.github.grantchan.ssh.arch.SshConstant;
import io.github.grantchan.ssh.arch.SshMessage;
import io.github.grantchan.ssh.common.userauth.service.Service;
import io.github.grantchan.ssh.common.userauth.service.ServiceFactories;
import io.github.grantchan.ssh.util.buffer.ByteBufIo;
import io.github.grantchan.ssh.util.buffer.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.util.List;

public class Session {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private ChannelHandlerContext ctx;

  private boolean isServer;
  private byte[] id;

  /*
   * RFC 4253:
   * Both the 'protoversion' and 'softwareversion' strings MUST consist of
   * printable US-ASCII characters, with the exception of whitespace
   * characters and the minus sign (-).
   */
  private String clientId = null;  // client identification
  private String serverId = null;  // server identification

  private byte[] c2sKex = null; // the payload of the client's SSH_MSG_KEXINIT
  private byte[] s2cKex = null; // the payload of the server's SSH_MSG_KEXINIT
  private List<String> kexInit;

  private Cipher c2sCipher, s2cCipher;
  private int c2sCipherSize = 8, s2cCipherSize = 8;

  private Mac c2sMac, s2cMac;
  private int c2sMacSize = 0, s2cMacSize = 0;
  private int c2sDefMacSize = 0, s2cDefMacSize = 0;

  private Service service;
  private String username;
  private String remoteAddr;

  // constructor
  public Session(ChannelHandlerContext ctx, boolean isServer) {
    this.ctx = ctx;
    this.isServer = isServer;
  }

  public boolean isServer() {
    return this.isServer;
  }

  public byte[] getId() {
    return id;
  }

  public void setId(byte[] id) {
    this.id = id;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getServerId() {
    return serverId;
  }

  public void setServerId(String serverId) {
    this.serverId = serverId;
  }

  public byte[] getC2sKex() {
    return c2sKex;
  }

  public void setC2sKex(byte[] c2sKex) {
    this.c2sKex = c2sKex;
  }

  public byte[] getS2cKex() {
    return s2cKex;
  }

  public void setS2cKex(byte[] s2cKex) {
    this.s2cKex = s2cKex;
  }

  public void setKexInit(List<String> kexInit) {
    this.kexInit = kexInit;
  }

  public List<String> getKexInit() {
    return kexInit;
  }

  public Cipher getC2sCipher() {
    return c2sCipher;
  }

  public void setC2sCipher(Cipher c2sCipher) {
    this.c2sCipher = c2sCipher;
  }

  public Cipher getS2cCipher() {
    return s2cCipher;
  }

  public void setS2cCipher(Cipher s2cCipher) {
    this.s2cCipher = s2cCipher;
  }

  public int getC2sCipherSize() {
    return c2sCipherSize;
  }

  public void setC2sCipherSize(int c2sCipherSize) {
    this.c2sCipherSize = c2sCipherSize;
  }

  public int getS2cCipherSize() {
    return s2cCipherSize;
  }

  public void setS2cCipherSize(int s2cCipherSize) {
    this.s2cCipherSize = s2cCipherSize;
  }

  public Mac getC2sMac() {
    return c2sMac;
  }

  public void setC2sMac(Mac c2sMac) {
    this.c2sMac = c2sMac;
  }

  public Mac getS2cMac() {
    return s2cMac;
  }

  public void setS2cMac(Mac s2cMac) {
    this.s2cMac = s2cMac;
  }

  public int getC2sMacSize() {
    return c2sMacSize;
  }

  public void setC2sMacSize(int c2sMacSize) {
    this.c2sMacSize = c2sMacSize;
  }

  public int getS2cMacSize() {
    return s2cMacSize;
  }

  public void setS2cMacSize(int s2cMacSize) {
    this.s2cMacSize = s2cMacSize;
  }

  public int getC2sDefMacSize() {
    return c2sDefMacSize;
  }

  public void setC2sDefMacSize(int c2sDefMacSize) {
    this.c2sDefMacSize = c2sDefMacSize;
  }

  public int getS2cDefMacSize() {
    return s2cDefMacSize;
  }

  public void setS2cDefMacSize(int s2cDefMacSize) {
    this.s2cDefMacSize = s2cDefMacSize;
  }

  /**
   * Sends a disconnection message to terminate the connection.
   * <p>This message causes immediate termination of the connection. All implementations MUST be
   * able to process this message; they SHOULD be able to send this message.</p>
   *
   * @param reason   the reason code, it gives the reason in a more machine-readable format,
   *                 it should be one of the value in the Disconnection Messages Reason Codes and
   *                 Descriptions section in {@link SshMessage}.
   * @param message  the Disconnection Message, it gives a more specific explanation in a
   *                 human-readable form
   *
   * @see <a href="https://tools.ietf.org/html/rfc4253#section-11.1">Disconnection Message</a>
   */
  public void disconnect(int reason, String message) {
    ByteBuf buf = createMessage(SshMessage.SSH_MSG_DISCONNECT);

    buf.writeInt(reason);
    ByteBufIo.writeUtf8(buf, message);
    ByteBufIo.writeUtf8(buf, "");

    ctx.channel().writeAndFlush(buf);
  }

  /**
   * Sends the {@link SshMessage#SSH_MSG_SERVICE_ACCEPT} message to the client to notify the client
   * the service can be supported, and permits to use.
   *
   * @param svcName  the service name requested by client
   *
   * @see <a href="https://tools.ietf.org/html/rfc4253#section-10">Service Request</a>
   */
  public void replyAccept(String svcName) {
    ByteBuf buf = createMessage(SshMessage.SSH_MSG_SERVICE_ACCEPT);

    ByteBufIo.writeUtf8(buf, svcName);

    logger.debug("[{}] Replying SSH_MSG_SERVICE_ACCEPT...", this);

    ctx.channel().writeAndFlush(buf);
  }

  public String getRemoteAddress() {
    if (remoteAddr == null) {
      InetSocketAddress isa = (InetSocketAddress) ctx.channel().remoteAddress();

      remoteAddr = isa.getAddress().getHostAddress();
    }
    return remoteAddr;
  }

  /**
   * Sends the {@link SshMessage#SSH_MSG_KEX_DH_GEX_GROUP} message to the client, along with p and g
   *
   * @param p  safe prime
   * @param g  generator for subgroup in GF(p)
   *
   * @see <a href="https://tools.ietf.org/html/rfc4419#section-3">Diffie-Hellman Group and Key Exchange</a>
   */
  public void replyDhGexGroup(BigInteger p, BigInteger g) {
    ByteBuf pg = createMessage(SshMessage.SSH_MSG_KEX_DH_GEX_GROUP);

    ByteBufIo.writeMpInt(pg, p);
    ByteBufIo.writeMpInt(pg, g);

    logger.debug("[{}] Replying SSH_MSG_KEX_DH_GEX_GROUP...", this);

    ctx.channel().writeAndFlush(pg);
  }

  /**
   * Sends the {@link SshMessage#SSH_MSG_NEWKEYS} message to the client to notify the key exchange
   * process ends.
   *
   * <p>This message is sent with the old keys and algorithms.  All messages sent after this message
   * MUST use the new keys and algorithms.</p>
   *
   * <p>When this message is received, the new keys and algorithms MUST be used for receiving.</p>
   *
   * <p>The purpose of this message is to ensure that a party is able to respond with an
   * {@link SshMessage#SSH_MSG_DISCONNECT} message that the other party can understand if something
   * goes wrong with the key exchange.</p>
   *
   * @see <a href="https://tools.ietf.org/html/rfc4253#section-7.3">Taking Keys Into Use</a>
   */
  public void requestKexNewKeys() {
    ByteBuf newKeys = createMessage(SshMessage.SSH_MSG_NEWKEYS);

    logger.debug("[{}] Requesting SSH_MSG_NEWKEYS...", this);

    ctx.channel().writeAndFlush(newKeys);
  }

  public ByteBuf createBuffer() {
    return ctx.alloc().buffer();
  }

  private ByteBuf createMessage(byte messageId) {
    ByteBuf msg = createBuffer();

    msg.writerIndex(SshConstant.SSH_PACKET_HEADER_LENGTH);
    msg.readerIndex(SshConstant.SSH_PACKET_HEADER_LENGTH);
    msg.writeByte(messageId);

    return msg;
  }

  /**
   * Sends the {@link SshMessage#SSH_MSG_USERAUTH_SUCCESS} message to client to notify the
   * authentication request is accepted.
   *
   * <p>Note that this is not sent after each step in a multi-method authentication sequence, but
   * only when the authentication is complete.</p>
   *
   * <p>The client MAY send several authentication requests without waiting for responses from
   * previous requests. The server MUST process each request completely and acknowledge any failed
   * requests with a {@link SshMessage#SSH_MSG_USERAUTH_FAILURE} message before processing the next
   * request.</p>
   *
   * <p>A request that requires further messages to be exchanged will be aborted by a subsequent
   * request. A client MUST NOT send a subsequent request if it has not received a response from
   * the server for a previous request. A {@link SshMessage#SSH_MSG_USERAUTH_FAILURE} message MUST
   * NOT be sent for an aborted method.</p>
   *
   * <p>{@link SshMessage#SSH_MSG_USERAUTH_SUCCESS} MUST be sent only once. When
   * {@link SshMessage#SSH_MSG_USERAUTH_SUCCESS} has been sent, any further authentication requests
   * received after that SHOULD be silently ignored.</p>
   *
   * <p>Any non-authentication messages sent by the client after the request that resulted in
   * {@link SshMessage#SSH_MSG_USERAUTH_SUCCESS} being sent MUST be passed to the service being run
   * on top of this protocol. Such messages can be identified by their message numbers
   * (see Section 6).</p>
   *
   * @see <a href="https://tools.ietf.org/html/rfc4252#section-5.1">Responses to Authentication Requests</a>
   */
  public void replyUserAuthSuccess() {
    ByteBuf uas = createMessage(SshMessage.SSH_MSG_USERAUTH_SUCCESS);

    logger.debug("[{}] Replying SSH_MSG_USERAUTH_SUCCESS...", this);

    ctx.channel().writeAndFlush(uas);
  }

  /**
   * Sends the SSH_MSG_USERAUTH_FAILURE message to client to reject the authentication request.
   *
   * <p>It is RECOMMENDED that servers only include those 'method name' values
   * in the name-list that are actually useful. However, it is not illegal to
   * include 'method name' values that cannot be used to authenticate the
   * user.</p>
   * <p>Already successfully completed authentications SHOULD NOT be included in
   * the name-list, unless they should be performed again for some reason.</p>
   *
   * @param remainMethods   a comma-separated name-list of authentication 'method name' values that
   *                        may productively continue the authentication dialog.
   * @param partialSuccess  MUST be {@code true} if the authentication request to which this is a
   *                        response was successful. It MUST be {@code FALSE} if the request was not
   *                        successfully processed.
   * @see <a href="https://tools.ietf.org/html/rfc4252#section-5.1">Responses to Authentication Requests</a>
   */
  public void replyUserAuthFailure(String remainMethods, boolean partialSuccess) {
    ByteBuf uaf = createMessage(SshMessage.SSH_MSG_USERAUTH_FAILURE);

    ByteBufIo.writeUtf8(uaf, remainMethods);
    uaf.writeBoolean(partialSuccess);

    logger.debug("[{}] Replying SSH_MSG_USERAUTH_FAILURE...", this);

    ctx.channel().writeAndFlush(uaf);
  }

  /**
   * Sends the SSH_MSG_USERAUTH_PK_OK message to tell client the pass phase in public key
   * authentication process is successful.
   *
   * @param algorithm  public key algorithm name that server received in SSH_MSG_USERAUTH_REQUEST
   * @param blob       public key blob that server received in SSH_MSG_USERAUTH_REQUEST
   * @see <a href="https://tools.ietf.org/html/rfc4252#section-7">Public Key Authentication Method: "publickey"</a>
   */
  public void replyUserAuthPkOk(String algorithm, byte[] blob) {
    ByteBuf uapo = createMessage(SshMessage.SSH_MSG_USERAUTH_PK_OK);

    ByteBufIo.writeUtf8(uapo, algorithm);
    ByteBufIo.writeBytes(uapo, blob);

    logger.debug("[{}] Replying SSH_MSG_USERAUTH_PK_OK...", this);

    ctx.channel().writeAndFlush(uapo);
  }

  public void replyKexDhReply(byte[] k_s, BigInteger f, byte[] sigH) {
    ByteBuf reply = createMessage(SshMessage.SSH_MSG_KEXDH_REPLY);

    ByteBufIo.writeBytes(reply, k_s);
    ByteBufIo.writeMpInt(reply, f);
    ByteBufIo.writeBytes(reply, sigH);

    logger.debug("[{}] Replying SSH_MSG_KEXDH_REPLY...", this);

    ctx.channel().writeAndFlush(reply);
  }

  /**
   * Sends the {@link SshMessage#SSH_MSG_KEX_DH_GEX_REPLY} to client. This is the message of step 4
   * in diffie-hellman group key exchange.
   *
   * @param k_s     server public host key and certificates (K_S)
   * @param f       f = g^y mod p, where y is a random number generated by server, 0 < y < (p-1)/2
   * @param sigH    signature of H
   *
   * @see <a href="https://tools.ietf.org/html/rfc4419#section-3">Diffie-Hellman Group and Key Exchange</a>
   */
  public void replyKexDhGexReply(byte[] k_s, BigInteger f, byte[] sigH) {
    ByteBuf reply = createMessage(SshMessage.SSH_MSG_KEX_DH_GEX_REPLY);

    ByteBufIo.writeBytes(reply, k_s);
    ByteBufIo.writeMpInt(reply, f);
    ByteBufIo.writeBytes(reply, sigH);

    logger.debug("[{}] Replying SSH_MSG_KEX_DH_GEX_REPLY...", this);

    ctx.channel().writeAndFlush(reply);
  }

  /**
   * Print a log information regarding the disconnect reason, disconnect the network channel
   *
   * @param code  the disconnect reason code
   * @param msg   message about the reason
   */
  public void handleDisconnect(int code, String msg) {
    logger.info("Disconnecting... reason: {}, msg: {}", SshMessage.disconnectReason(code), msg);

    ctx.channel().close();
  }

  /**
   * Create a {@link Service} instance by a given name
   * @param name          name of the service to create
   * @throws SshException  if the given name of service is not supported
   */
  public void acceptService(String name) throws SshException {
    service = ServiceFactories.create(name, this);
    if (service == null) {
      logger.info("Requested service ({}) from {} is unavailable, rejected.",
          name, getRemoteAddress());

      throw new SshException(SshMessage.SSH_DISCONNECT_SERVICE_NOT_AVAILABLE,
          "Bad service requested - '" + name + "'");
    }
  }

  public Service getService() {
    return this.service;
  }

  /**
   * Sends the {@link SshMessage#SSH_MSG_KEXDH_INIT} message to the server
   * @param e the public key generated by client, e = g ^ x mod p, where x is the client's private
   *          key.
   */
  public void requestKexDhInit(BigInteger e) {
    ByteBuf req = createMessage(SshMessage.SSH_MSG_KEXDH_INIT);

    ByteBufIo.writeMpInt(req, e);

    logger.debug("[{}] Requesting SSH_MSG_KEXDH_INIT...", this);

    ctx.channel().writeAndFlush(req);
  }

  /**
   * Sends the {@link SshMessage#SSH_MSG_SERVICE_REQUEST} message to the server
   */
  public void requestServiceRequest() {
    ByteBuf req = createMessage(SshMessage.SSH_MSG_SERVICE_REQUEST);

    ByteBufIo.writeUtf8(req, "ssh-userauth");

    logger.debug("[{}] Requesting SSH_MSG_SERVICE_REQUEST...", this);

    ctx.channel().writeAndFlush(req);
  }

  public void requestUserAuthRequest(String user, String service, String method) {
    ByteBuf req = createMessage(SshMessage.SSH_MSG_USERAUTH_REQUEST);

    ByteBufIo.writeUtf8(req, user);
    ByteBufIo.writeUtf8(req, service);
    ByteBufIo.writeUtf8(req, method);

    logger.debug("[{}] Requesting SSH_MSG_USERAUTH_REQUEST... username:{}, service:{}, method:{}",
        this, user, service, method);

    ctx.channel().writeAndFlush(req);
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
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

    logger.debug("[{}] Requesting SSH_MSG_USERAUTH_REQUEST... " +
                 "username:{}, service:{}, method:{}, algo:{}", this, user, service, method, algo);

    ctx.channel().writeAndFlush(req);
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

    logger.debug("[{}] Requesting SSH_MSG_USERAUTH_REQUEST... " +
        "username:{}, service:{}, method:{}, algo:{}, sigature: {}", this, user, service, method, algo,
        Bytes.md5(sig));

    ctx.channel().writeAndFlush(req);
  }

  @Override
  public String toString() {
    return getUsername() + "@" + getRemoteAddress();
  }
}
