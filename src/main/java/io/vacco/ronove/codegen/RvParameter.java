package io.vacco.ronove.codegen;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class RvParameter {

  public String name;
  public String tsType;
  public Type type;
  public Annotation paramType;

  @Override public String toString() {
    return String.format(
        "(%s) %s: %s",
        paramType != null ? paramType.annotationType().getSimpleName() : "?",
        name, tsType
    );
  }
}
