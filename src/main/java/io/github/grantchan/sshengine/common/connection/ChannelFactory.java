package io.github.grantchan.sshengine.common.connection;

import io.github.grantchan.sshengine.common.AbstractSession;

@FunctionalInterface
public interface ChannelFactory {

  /**
   * @return a new Channel instance
   */
  Channel create(AbstractSession session);
}
