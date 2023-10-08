package io.vacco.ronove;

import java.util.*;

public class RvTsType {

  public String name, type;
  public Set<String> enumValues = new LinkedHashSet<>();
  public List<RvTsType> properties = new ArrayList<>();

  @Override public String toString() {
    return String.format("%s: %s", name, type);
  }

}
