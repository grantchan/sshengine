package io.github.grantchan.ssh.trans.digest;

import io.github.grantchan.ssh.common.NamedFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public enum BuiltinDigestFactory implements NamedFactory<MessageDigest> {

  sha1("SHA-1"),
  sha256("SHA-256"),
  sha512("SHA-512");

  private String name;

  BuiltinDigestFactory(String name) {
    this.name = name;
  }

  @Override
  public MessageDigest create() {
    try {
      return MessageDigest.getInstance(name);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public String getName() {
    return this.name;
  }
}
