package io.vacco.ronove;

import jakarta.ws.rs.DefaultValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Defines the relationship between a Java method's arguments and
 * the way they deserialize from HTTP requests.
 */
public class RvParameter {

  public int    position;
  public String name;
  public Type   type;

  public Annotation   paramType;
  public DefaultValue defaultValue;

  @Override public String toString() {
    return String.format(
        "[%s] (%s) %s",
        position,
        paramType != null ? paramType.annotationType().getSimpleName() : "?",
        name
    );
  }

}
