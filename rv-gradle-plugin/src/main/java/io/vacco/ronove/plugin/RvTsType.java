package io.vacco.ronove.plugin;

import java.util.*;

/**
 * Holds metadata for either 1) TS interfaces or 2) TS enums.
 * Nothing more (for now).
 */
public class RvTsType {

  public String name, type;
  public Set<String> enumValues = new LinkedHashSet<>();
  public List<RvTsType> properties = new ArrayList<>();

  public RvTsType(String name, String type) {
    this.name = name;
    this.type = Objects.requireNonNull(type);
  }

  public RvTsType withName(String name) {
    this.name = name;
    return this;
  }

  @Override public String toString() {
    return String.format("%s: %s", name, type);
  }

}