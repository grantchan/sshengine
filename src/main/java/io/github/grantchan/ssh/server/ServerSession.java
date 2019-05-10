package io.github.grantchan.ssh.server;

import io.github.grantchan.ssh.arch.SshMessage;
import io.github.grantchan.ssh.common.Session;
import io.github.grantchan.ssh.util.buffer.ByteBufIo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

public class ServerSession extends Session {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public ServerSession(ChannelHandlerContext ctx) {
    super(ctx, true);
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


}
