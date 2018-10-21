package io.github.grantchan.ssh.trans.compression;

import io.github.grantchan.ssh.common.NamedObject;
import io.github.grantchan.ssh.common.NamedFactory;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum BuiltinCompressionFactory implements NamedFactory<Compression> {

  none("none");

  private static final Set<BuiltinCompressionFactory> values =
      Collections.unmodifiableSet(EnumSet.allOf(BuiltinCompressionFactory.class));

  public static String getNames() {
    return NamedObject.getNames(BuiltinCompressionFactory.values);
  }

  private final String name;

  BuiltinCompressionFactory(String name) {
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
}