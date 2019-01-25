package io.github.grantchan.ssh.common.transport.compression;

import io.github.grantchan.ssh.common.NamedFactory;
import io.github.grantchan.ssh.common.NamedObject;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum CompressionFactories implements NamedFactory<Compression> {

  none("none");

  private static final Set<CompressionFactories> values =
      Collections.unmodifiableSet(EnumSet.allOf(CompressionFactories.class));

  private final String name;

  CompressionFactories(String name) {
    this.name = name;
  }

  @Override
  public Compression create() {
    return null;
  }

  @Override
  public String getName() {
    return this.name;
  }

  public static String getNames() {
    return NamedObject.getNames(values);
  }
}