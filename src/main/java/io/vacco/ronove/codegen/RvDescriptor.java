package io.vacco.ronove.codegen;

import javax.ws.rs.Path;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class RvDescriptor {

  public Method handler;
  public Annotation httpMethod;
  public String httpMethodTxt;
  public Path path;
  public String responseTsType;
  public List<RvParameter> parameters = new ArrayList<>();

  @Override public String toString() {
    return String.format("(%s) %s (%s) -> %s", httpMethodTxt, path, parameters.size(), responseTsType);
  }
}
