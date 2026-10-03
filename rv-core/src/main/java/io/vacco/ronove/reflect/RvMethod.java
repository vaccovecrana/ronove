package io.vacco.ronove.reflect;

import io.vacco.ronove.api.RvStatus;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Defines a relationship between a Java method and the
 * source of its parameters. For example, a method could
 * expect one Query string parameter, and one Header parameter
 * originating from an incoming HTTP request.
 */
public class RvMethod {

  public Method javaMethod;
  public Annotation httpMethod;
  public Consumes consumes;
  public Produces produces;
  public String httpMethodTxt;
  public RvStatus httpStatus;
  public Path path;
  public RvType responseType;

  public RvParameter beanParam;
  public List<RvParameter> pathParams = new ArrayList<>();
  public List<RvParameter> queryParams = new ArrayList<>();
  public List<RvParameter> cookieParams = new ArrayList<>();
  public List<RvParameter> formParams = new ArrayList<>();
  public List<RvParameter> headerParams = new ArrayList<>();
  public List<RvParameter> attachmentParams = new ArrayList<>();
  public List<RvParameter> allParams = new ArrayList<>();

  public String id() {
    return String.format("(%s) %s", httpMethodTxt, path);
  }

  @Override
  public String toString() {
    return id();
  }

  public Stream<RvType> allTypes() {
    return Stream.concat(
      Stream.of(responseType),
      allParams.stream().map(rp -> rp.type)
    );
  }

}
