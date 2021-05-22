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

  public RvParameter beanParam;
  public List<RvParameter> queryParams = new ArrayList<>();
  public List<RvParameter> headerParams = new ArrayList<>();
  public List<RvParameter> pathParams = new ArrayList<>();

  @Override public String toString() {
    return String.format("(%s) %s -> %s", httpMethodTxt, path, responseTsType);
  }
}
