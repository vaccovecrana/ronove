package io.vacco.ronove;

import jakarta.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Defines a relationship between a Java method, and the
 * source of its parameters. For example, a method could
 * expect one Query string parameter, and one Header parameter
 * originating from an incoming HTTP request.
 */
public class RvDescriptor {

  public Method     javaMethod;
  public Annotation httpMethod;
  public Consumes   consumes;
  public Produces   produces;
  public String     httpMethodTxt;
  public RvStatus   httpStatus;
  public Path       path;
  public String     responseTsType;
  public String     paramsTsList;

  public RvParameter        beanParam;
  public List<RvParameter>  pathParams        = new ArrayList<>();
  public List<RvParameter>  queryParams       = new ArrayList<>();
  public List<RvParameter>  cookieParams      = new ArrayList<>();
  public List<RvParameter>  formParams        = new ArrayList<>();
  public List<RvParameter>  headerParams      = new ArrayList<>();
  public List<RvParameter>  attachmentParams  = new ArrayList<>();
  public List<RvParameter>  allParams         = new ArrayList<>();

  @Override public String toString() {
    return String.format("(%s) %s => %s", httpMethodTxt, path, responseTsType);
  }

}
