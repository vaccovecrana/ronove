package io.vacco.ronove.reflect;

import java.lang.reflect.Type;
import java.util.*;

import static java.lang.String.format;

/**
 * Holds metadata for either 1) TS interfaces or 2) TS enums.
 * Nothing else (for now).
 */
public class RvType {

  public String declaration, name;
  public RvType extendz;
  public Set<String> enumValues = new LinkedHashSet<>();
  public Map<String, RvType> properties = new LinkedHashMap<>();
  public Type from;
  public boolean rpc = true;

  public RvType(String declaration, String name, Type from) {
    this.declaration = declaration;
    this.name = name;
    this.from = Objects.requireNonNull(from);
  }

  @Override
  public String toString() {
    return format(
      "%s %s [%s] %s",
      declaration, name, from,
      extendz != null ? format(" <- %s", extendz) : ""
    );
  }

  @Override
  public boolean equals(Object obj) {
    return
      obj instanceof RvType
        && ((RvType) obj).from.equals(this.from);
  }

  @Override
  public int hashCode() {
    return this.from.hashCode();
  }

}