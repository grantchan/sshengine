package io.github.grantchan.sshengine.server.transport.handler;

import io.github.grantchan.sshengine.common.AbstractSession;
import io.github.grantchan.sshengine.common.transport.compression.Compression;
import io.github.grantchan.sshengine.common.transport.handler.AbstractPacketEncoder;
import io.github.grantchan.sshengine.server.ServerSession;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import java.util.Objects;

public class ServerPacketEncoder extends AbstractPacketEncoder {

  private final ServerSession session;

  public ServerPacketEncoder(ServerSession session) {
    this.session = Objects.requireNonNull(session, "Session is not initialized");
  }

  @Override
  public AbstractSession getSession() {
    return session;
  }

  @Override
  public Cipher getCipher() {
    return session.getS2cCipher();
  }

  @Override
  public int getCipherSize() {
    return session.getS2cCipherSize();
  }

  @Override
  public Mac getMac() {
    return session.getS2cMac();
  }

  @Override
  public int getMacSize() {
    return session.getS2cMacSize();
  }

  @Override
  public int getDefMacSize() {
    return session.getS2cDefMacSize();
  }

  @Override
  public Compression getCompression() {
    return session.getS2cCompression();
  }
}