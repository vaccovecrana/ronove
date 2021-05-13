package io.vacco.ronove.codegen;

import javax.ws.rs.Path;
import java.lang.reflect.Method;

public class RvDescriptor {
  public String tsInputType;
  public String tsOutputType;
  public Method opMethod;
  public String httpMethod;
  public Path path;
}
