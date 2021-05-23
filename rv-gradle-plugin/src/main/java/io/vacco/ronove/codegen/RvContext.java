package io.vacco.ronove.codegen;

import io.marioslab.basis.template.*;
import io.vacco.oruzka.core.OFnSupplier;
import org.gradle.api.logging.*;

import jakarta.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

import static io.vacco.ronove.codegen.RvAnnotations.*;
import static java.lang.String.format;

public class RvContext {

  private static final Logger log = Logging.getLogger(RvContext.class);

  public final Map<String, RvDescriptor> paths = new TreeMap<>();
  public final RvTypeFactory df = new RvTypeFactory();

  public RvParameter describe(Parameter p) {
    return OFnSupplier.tryGet(() -> {
      RvParameter rp = new RvParameter();
      Type t = p.getParameterizedType();
      Annotation pt = paramTypeOf(p);
      Method value = pt instanceof BeanParam ? null : pt.getClass().getMethod("value");
      rp.paramType = pt;
      rp.name = value != null ? value.invoke(rp.paramType).toString() : null;
      rp.type = t;
      rp.tsType = t instanceof ParameterizedType ? df.tsArgsOf((ParameterizedType) t) : df.tsTypeOf(t);
      return rp;
    });
  }

  public RvDescriptor describe(Method m, Path p, Annotation jaxRsMethod) {
    String mTxt = String.format("Method: (%s) %s %s", jaxRsMethod, p, m);
    log.warn(mTxt);
    RvDescriptor d = new RvDescriptor();
    d.path = p;
    d.handler = m;
    d.httpMethod = jaxRsMethod;
    d.httpMethodTxt = jaxRsMethod.toString().replace("@jakarta.ws.rs.", "").replace("()", "");
    d.responseTsType = df.tsArgsOf((ParameterizedType) m.getGenericReturnType());
    d.allParams = Arrays.stream(m.getParameters()).map(this::describe).collect(Collectors.toList());
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
          throw new IllegalArgumentException(format(
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

  public Map<String, RvDescriptor> contextFor(List<Class<?>> controllers) {
    for (Class<?> ct : controllers) {
      for (Method m : ct.getMethods()) {
        Optional<Annotation> op = Arrays.stream(m.getAnnotations()).filter(an -> Path.class.isAssignableFrom(an.getClass())).findFirst();
        Optional<Annotation> ojxm = Arrays.stream(m.getAnnotations()).filter(RvAnnotations::isJaxRsMethod).findFirst();
        if (op.isPresent() && ojxm.isPresent()) {
          RvDescriptor rd = describe(m, (Path) op.get(), ojxm.get());
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

  public String render(List<Class<?>> controllers) {
    log.warn("Generating RPC client from definitions: {}", controllers);
    TemplateContext context = new TemplateContext();
    TemplateLoader loader = new TemplateLoader.ClasspathTemplateLoader();
    Template template = loader.load("/io/vacco/ronove/codegen/rv-ts-rpc.bt");
    Map<String, RvDescriptor> ctx = contextFor(controllers);

    context.set("rvControllers", controllers.stream().map(Class::getCanonicalName).collect(Collectors.toList()));
    context.set("rvDescriptors", ctx.values());
    context.set("tsSchemaTypes", String.join(", ", df.tsSchemaTypes));
    return template.render(context);
  }

}
