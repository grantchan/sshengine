package io.github.grantchan.sshengine.client.connection;

import io.github.grantchan.sshengine.common.CommonState;
import io.github.grantchan.sshengine.common.connection.Channel;
import io.github.grantchan.sshengine.common.connection.SshChannelException;
import io.netty.buffer.ByteBuf;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface ClientChannel extends Channel, CommonState {

  /**
   * Returns the type of this channel, it could be a shell, a system command, or built-in subsystem
   *
   * @return "shell" or "exec" or "subsystem"
   */
  String getType();

  void handleOpenConfirmation(ByteBuf req);

  void handleOpenFailure(ByteBuf req);

  void waitFor(State state, long timeout, TimeUnit unit);

  CompletableFuture<ClientChannel> open() throws SshChannelException;

  void setIn(InputStream in);

  void setOut(OutputStream out);

  void setErr(OutputStream err);
}
