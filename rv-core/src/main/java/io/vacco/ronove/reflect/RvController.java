package io.vacco.ronove.reflect;

import io.vacco.ronove.api.RvAttachmentParam;
import io.vacco.ronove.api.RvGraal;
import io.vacco.ronove.api.RvStatus;
import jakarta.ws.rs.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static io.vacco.ronove.reflect.RvAnnotations.*;
import static java.lang.String.format;

public class RvController {

  public final Class<?> clazz;
  public final Map<String, RvMethod> methods;
  public final RvGraal graal;
  private final RvTypeCache typeCache;

  public RvController(Class<?> clazz, RvTypeCache typeCache) {
    this.clazz = Objects.requireNonNull(clazz);
    this.typeCache = Objects.requireNonNull(typeCache);
    this.graal = clazz.getAnnotation(RvGraal.class);
    this.methods = describe(clazz);
  }

  private RvParameter describe(Parameter p, int position) throws Exception {
    var rp = new RvParameter();
    var t = typeCache.get(p.getParameterizedType());
    var pt = paramTypeOf(p);
    var pName = isJaxRsBodyParam(pt)
      ? p.getName()
      : pt.getClass().getMethod("value").invoke(pt).toString();
    rp.position = position;
    rp.paramType = pt;
    rp.name = pName;
    rp.type = t;
    defaultValueOf(p).ifPresent(dv -> rp.defaultValue = dv);
    return rp;
  }

  private RvMethod describe(Method m, Path p, Annotation jxRsMethod,
                            Consumes jxRsConsumes, Produces jxRsProduces,
                            RvStatus rvStatus) {
    try {
      var d = new RvMethod();
      d.path = p;
      d.javaMethod = m;
      d.responseType = typeCache.get(m.getGenericReturnType());
      d.httpStatus = rvStatus;
      d.consumes = jxRsConsumes;
      d.produces = jxRsProduces;
      d.httpMethod = jxRsMethod;
      d.httpMethodTxt = jxRsMethod.toString()
        .replace("@jakarta.ws.rs.", "")
        .replace("()", "");

      var parameters = new ArrayList<RvParameter>();
      for (int i = 0; i < m.getParameters().length; i++) {
        var rvp = describe(m.getParameters()[i], i);
        parameters.add(i, rvp);
      }

      d.allParams = parameters;
      var parIdx = d.allParams.stream()
        .collect(Collectors.groupingBy(prm -> prm.paramType.annotationType().getSimpleName()));

      if (parIdx.get(PathParam.class.getSimpleName()) != null) {
        d.pathParams = parIdx.get(PathParam.class.getSimpleName());
        d.pathParams.forEach(pp -> {
          if (!d.path.value().contains(pp.name)) {
            throw new IllegalArgumentException(format(
              "Path parameter definition [%s] not found in controller path [%s]. Ensure parameter names match.",
              pp.name, d.path.value()
            ));
          }
        });
      }
      if (parIdx.get(QueryParam.class.getSimpleName()) != null) {
        d.queryParams = parIdx.get(QueryParam.class.getSimpleName());
      }
      if (parIdx.get(CookieParam.class.getSimpleName()) != null) {
        d.cookieParams = parIdx.get(CookieParam.class.getSimpleName());
      }
      if (parIdx.get(FormParam.class.getSimpleName()) != null) {
        d.formParams = parIdx.get(FormParam.class.getSimpleName());
      }
      if (parIdx.get(HeaderParam.class.getSimpleName()) != null) {
        d.headerParams = parIdx.get(HeaderParam.class.getSimpleName());
      }
      if (parIdx.get(BeanParam.class.getSimpleName()) != null) {
        var bpList = parIdx.get(BeanParam.class.getSimpleName());
        if (bpList.size() > 1) {
          throw new IllegalStateException(format(
            "Multiple bean parameters defined: %s", bpList
          ));
        }
        d.beanParam = bpList.get(0);
      }
      if (parIdx.get(RvAttachmentParam.class.getSimpleName()) != null) {
        d.attachmentParams = parIdx.get(RvAttachmentParam.class.getSimpleName());
      }
      if (isNonBodyJaxRsMethod(d.httpMethod)
        && (d.beanParam != null || !d.formParams.isEmpty())) {
        throw new IllegalStateException(format(
          "Method [%s] cannot define Bean or Form parameters. %s %s",
          m, d.beanParam, d.formParams
        ));
      }
      if (d.beanParam != null && !d.formParams.isEmpty()) {
        throw new IllegalStateException(format(
          "Method [%s] cannot define both Bean and Form parameters. %s %s",
          m, d.beanParam, d.formParams
        ));
      }
      return d;
    } catch (Exception e) {
      throw new IllegalStateException(format(
        "Unable to map method [%s] with path [%s]", m, p
      ), e);
    }
  }

  private Map<String, RvMethod> describe(Class<?> ct) {
    var out = new TreeMap<String, RvMethod>();
    for (var m : ct.getMethods()) {
      var op = Arrays.stream(m.getAnnotations()).filter(RvAnnotations::isJaxRsPath).findFirst();
      var oJxm = Arrays.stream(m.getAnnotations()).filter(RvAnnotations::isJaxRsMethod).findFirst();
      var oJxc = Arrays.stream(m.getAnnotations()).filter(RvAnnotations::isJaxRsConsumes).findFirst();
      var oJxp = Arrays.stream(m.getAnnotations()).filter(RvAnnotations::isJaxRsProduces).findFirst();
      var oRvStat = Arrays.stream(m.getAnnotations()).filter(RvAnnotations::isRvStatus).findFirst();
      if (op.isPresent() && oJxm.isPresent()) {
        var rd = describe(
          m, (Path) op.get(), oJxm.get(),
          (Consumes) oJxc.orElse(null),
          (Produces) oJxp.orElse(null),
          (RvStatus) oRvStat.orElse(null)
        );
        if (out.containsKey(rd.id())) {
          var rd1 = out.get(rd.id());
          throw new IllegalStateException(format(
            "Request path [%s] mapped by methods [%s] and [%s]",
            rd.id(), rd.javaMethod, rd1.javaMethod
          ));
        }
        out.put(rd.id(), rd);
      }
    }
    if (out.isEmpty()) {
      throw new IllegalStateException(format(
        "Controller class [%s] describes no REST methods", ct
      ));
    }
    return out;
  }

  @Override
  public String toString() {
    return clazz.getCanonicalName();
  }

}
