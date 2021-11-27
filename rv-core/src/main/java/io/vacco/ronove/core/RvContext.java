package io.vacco.ronove.core;

import jakarta.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

import static io.vacco.ronove.core.RvAnnotations.*;
import static java.lang.String.format;

public class RvContext {

  public final Map<String, RvDescriptor> paths = new TreeMap<>();
  public final RvTypescriptFactory tsFactory = new RvTypescriptFactory();

  public RvParameter describe(Parameter p, int position) {
    try {
      RvParameter rp = new RvParameter();
      Type t = p.getParameterizedType();
      Annotation pt = paramTypeOf(p);
      Method value = pt instanceof BeanParam ? null : pt.getClass().getMethod("value");
      rp.position = position;
      rp.paramType = pt;
      rp.name = value != null ? value.invoke(rp.paramType).toString() : null;
      rp.type = t;
      rp.tsType = t instanceof ParameterizedType ? tsFactory.tsArgsOf((ParameterizedType) t) : tsFactory.tsTypeOf(t);
      defaultValueOf(p).ifPresent(dv -> rp.defaultValue = dv);
      return rp;
    } catch (Exception e) {
      throw new IllegalStateException(format("Unable to map parameter [%s]", p), e);
    }
  }

  public RvDescriptor describe(Method m, Path p, Annotation jxRsMethod, RvStatus rvStatus) {
    RvDescriptor d = new RvDescriptor();
    d.path = p;
    d.handler = m;
    d.httpMethod = jxRsMethod;
    d.httpMethodTxt = jxRsMethod.toString().replace("@jakarta.ws.rs.", "").replace("()", "");
    d.httpStatus = rvStatus;
    d.responseTsType = tsFactory.tsReturnTypeOf(m);

    List<RvParameter> parameters = new ArrayList<>();
    for (int i = 0; i < m.getParameters().length; i++) {
      RvParameter rvp = describe(m.getParameters()[i], i);
      parameters.add(i, rvp);
    }

    d.allParams = parameters;
    d.paramsTsList = d.allParams.stream()
        .map(prm -> format("%s: %s", prm.name != null ? prm.name : "body", prm.tsType))
        .collect(Collectors.joining(", "));

    Map<String, List<RvParameter>> parmIdx = d.allParams.stream().collect(Collectors.groupingBy(prm -> prm.paramType.annotationType().getSimpleName()));

    if (parmIdx.get(BeanParam.class.getSimpleName()) != null) {
      d.beanParam = parmIdx.get(BeanParam.class.getSimpleName()).get(0);
    }
    if (parmIdx.get(QueryParam.class.getSimpleName()) != null) {
      d.queryParams = parmIdx.get(QueryParam.class.getSimpleName());
    }
    if (parmIdx.get(HeaderParam.class.getSimpleName()) != null) {
      d.headerParams = parmIdx.get(HeaderParam.class.getSimpleName());
    }
    if (parmIdx.get(PathParam.class.getSimpleName()) != null) {
      d.pathParams = parmIdx.get(PathParam.class.getSimpleName());
      d.pathParams.forEach(pp -> {
        if (!d.path.value().contains(pp.name)) {
          throw new IllegalArgumentException(String.format(
              "Path parameter definition [%s] not found in controller path [%s]. Ensure parameter names match.",
              pp.name, d.path.value()
          ));
        }
      });
    }
    if (isNonBodyJaxRsMethod(d.httpMethod) && d.beanParam != null) {
      throw new IllegalStateException(String.format("Method %s cannot define body (bean) parameter %s", m, d.beanParam));
    }

    return d;
  }

  public Map<String, RvDescriptor> describe(List<Class<?>> controllers) {
    for (Class<?> ct : controllers) {
      for (Method m : ct.getMethods()) {
        Optional<Annotation> op = Arrays.stream(m.getAnnotations()).filter(RvAnnotations::isJaxRsPath).findFirst();
        Optional<Annotation> oJxm = Arrays.stream(m.getAnnotations()).filter(RvAnnotations::isJaxRsMethod).findFirst();
        Optional<Annotation> oRvStat = Arrays.stream(m.getAnnotations()).filter(RvAnnotations::isRvStatus).findFirst();
        if (op.isPresent() && oJxm.isPresent()) {
          RvDescriptor rd = describe(m, (Path) op.get(), oJxm.get(), (RvStatus) oRvStat.orElse(null));
          String path = rd.path.value();
          if (paths.containsKey(path)) {
            throw new RvException.RvDuplicateMappingException(path, rd, paths.get(path));
          }
          paths.put(path, rd);
        }
      }
    }
    return paths;
  }

  public Map<String, RvDescriptor> describe(Class<?> controller) {
    return describe(Collections.singletonList(controller));
  }

}
