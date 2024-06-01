package io.vacco.ronove;

import jakarta.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

import static io.vacco.ronove.RvAnnotations.*;
import static java.lang.String.format;

public class RvContext {

  public final Map<String, RvDescriptor> paths = new TreeMap<>();

  public RvParameter describe(Parameter p, int position) throws Exception {
    var rp = new RvParameter();
    var t = p.getParameterizedType();
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

  public RvDescriptor describe(Method m, Path p,
                               Annotation jxRsMethod,
                               Consumes jxRsConsumes,
                               Produces jxRsProduces,
                               RvStatus rvStatus) {
    try {
      var d = new RvDescriptor();
      d.path = p;
      d.javaMethod = m;
      d.responseType = m.getGenericReturnType();
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

  public Map<String, RvDescriptor> describe(List<Class<?>> controllers) {
    for (var ct : controllers) {
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
          var pathKey = format("%s:%s", rd.httpMethodTxt, rd.path.value());
          if (paths.containsKey(pathKey)) {
            throw new IllegalStateException(format(
              "Request path [%s] mapped multiple times by [%s] and [%s]",
              pathKey, rd, paths.get(pathKey)
            ));
          }
          paths.put(pathKey, rd);
        }
      }
    }
    if (paths.isEmpty()) {
      throw new IllegalStateException(format(
        "No handler methods found for controller classes: %s", controllers
      ));
    }
    return paths;
  }

  public Map<String, RvDescriptor> describe(Class<?> controller) {
    return describe(Collections.singletonList(controller));
  }

}
