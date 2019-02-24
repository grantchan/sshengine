package io.github.grantchan.ssh.client.userauth;

import io.github.grantchan.ssh.arch.SshMessage;
import io.github.grantchan.ssh.common.Session;
import io.github.grantchan.ssh.common.userauth.method.Method;
import io.github.grantchan.ssh.common.userauth.method.MethodFactories;
import io.github.grantchan.ssh.common.userauth.service.Service;
import io.github.grantchan.ssh.util.buffer.ByteBufIo;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class ClientUserAuthService implements Service {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private Session session;

  private String[] builtinMethods;
  private int currentMethodIdx = 0;
  private Method auth;

  public ClientUserAuthService(Session session) {
    this.session = session;

    builtinMethods = MethodFactories.getNames().split(",");
    if (builtinMethods.length == 0) {
      throw new RuntimeException("No authentication method available");
    }

    logger.debug("[{}@{}] Builtin authentication methods for client - {}",
        session.getUsername(), session.getRemoteAddress(), String.join(",", builtinMethods));
  }

  @Override
  public void handleMessage(int cmd, ByteBuf req) throws Exception {
    String user = session.getUsername();
    String remoteAddr = session.getRemoteAddress();

    if (cmd == SshMessage.SSH_MSG_USERAUTH_SUCCESS) {

      /*
       * Responses to Authentication Requests
       *
       * When the server accepts authentication, it MUST respond with the
       * following:
       *
       * byte      SSH_MSG_USERAUTH_SUCCESS
       *
       * Note that this is not sent after each step in a multi-method
       * authentication sequence, but only when the authentication is
       * complete.
       *
       * @see <a href="https://tools.ietf.org/html/rfc4252#section-5.1">Responses to Authentication Requests</a>
       *
       *
       * Completion of User Authentication
       *
       * Authentication is complete when the server has responded with
       * SSH_MSG_USERAUTH_SUCCESS.  All authentication related messages
       * received after sending this message SHOULD be silently ignored.
       *
       * After sending SSH_MSG_USERAUTH_SUCCESS, the server starts the
       * requested service.
       *
       * @see <a href="https://tools.ietf.org/html/rfc4252#section-5.3">Completion of User Authentication</a>
       */
      logger.debug("[{}@{}] User authentication succeeded.", user, remoteAddr);

      session.acceptService("ssh-connection");
    } else if (cmd == SshMessage.SSH_MSG_USERAUTH_FAILURE) {

      /*
       * If the server rejects the authentication request, it MUST respond
       * with the following:
       *
       * byte         SSH_MSG_USERAUTH_FAILURE
       * name-list    authentications that can continue
       * boolean      partial success
       *
       * The 'authentications that can continue' is a comma-separated name-
       * list of authentication 'method name' values that may productively
       * continue the authentication dialog.
       *
       * It is RECOMMENDED that servers only include those 'method name'
       * values in the name-list that are actually useful.  However, it is not
       * illegal to include 'method name' values that cannot be used to
       * authenticate the user.
       *
       * Already successfully completed authentications SHOULD NOT be included
       * in the name-list, unless they should be performed again for some
       * reason.
       *
       * The value of 'partial success' MUST be TRUE if the authentication
       * request to which this is a response was successful.  It MUST be FALSE
       * if the request was not successfully processed.
       *
       * @see <a href="https://tools.ietf.org/html/rfc4252#section-5.1">Responses to Authentication Requests</a>
       */
      String methods = ByteBufIo.readUtf8(req);
      boolean partial = req.readBoolean();

      logger.debug("[{}@{}] Received SSH_MSG_USERAUTH_FAILURE - methods={}, partial={}",
          user, remoteAddr, methods, partial);

      List<String> serverMethods = Arrays.asList(methods.split(","));

//      while(true) {
//        if (auth == null) {
//          logger.debug("Starting authentication process");
//        } else if (!auth.authenticate(user, "ssh-connection", req, session)) {
//          auth = null;
//
//          currentMethodIdx++;
//        } else {
//          logger.debug("[{}@{}] Authentication process ended successfully - method={}",
//              user, remoteAddr, builtinMethods[currentMethodIdx]);
//          return;
//        }
//
//        String method = null;
//        while (currentMethodIdx < builtinMethods.length) {
//          if (serverMethods.contains(builtinMethods[currentMethodIdx])) {
//            method = builtinMethods[currentMethodIdx];
//            break;
//          }
//          currentMethodIdx++;
//        }
//
//        if (method == null) {
//          logger.debug("[{}@{}] No more authentication method available - client={}, server={}." +
//                  " Authentication failed.", user, remoteAddr, String.join(",", builtinMethods),
//                  methods);
//          throw new SshException(SshMessage.SSH_DISCONNECT_NO_MORE_AUTH_METHODS_AVAILABLE,
//              "No more authentication method available, authentication failed");
//        }
//
//        auth = MethodFactories.create(method);
//      }

    }
  }
}
