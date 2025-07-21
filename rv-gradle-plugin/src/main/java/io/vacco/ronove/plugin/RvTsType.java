package io.vacco.ronove.plugin;

import java.lang.reflect.Type;
import java.util.*;

import static java.lang.String.format;

/**
 * Holds metadata for either 1) TS interfaces or 2) TS enums.
 * Nothing else (for now).
 */
public class RvTsType {

  public String name, type;
  public RvTsType extendz;
  public Set<String> enumValues = new LinkedHashSet<>();
  public List<RvTsType> properties = new ArrayList<>();
  public Type from;

  public RvTsType(String name, String type, Type from) {
    this.name = name;
    this.type = Objects.requireNonNull(type);
    this.from = Objects.requireNonNull(from);
  }

  public RvTsType withName(String name) {
    this.name = name;
    return this;
  }

  @Override public String toString() {
    return format(
      "%s: %s%s",
      name, type,
      extendz != null ? format(" <- %s", extendz) : ""
    );
  }

  @Override public boolean equals(Object obj) {
    return
      obj instanceof RvTsType
      && ((RvTsType) obj).from.equals(this.from);
  }

  @Override public int hashCode() {
    return this.from.hashCode();
  }

}