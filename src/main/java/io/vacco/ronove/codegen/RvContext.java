package io.vacco.ronove.codegen;

import io.marioslab.basis.template.*;
import io.vacco.oruzka.core.OFnSupplier;
import org.gradle.api.logging.*;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

import static io.vacco.ronove.codegen.RvAnnotations.*;

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
    d.httpMethodTxt = jaxRsMethod.toString().replace("@javax.ws.rs.", "").replace("()", "");
    d.responseTsType = df.tsArgsOf((ParameterizedType) m.getGenericReturnType());
    d.parameters = Arrays.stream(m.getParameters()).map(this::describe).collect(Collectors.toList());

    Optional<RvParameter> bodyParam = d.parameters.stream().filter(pr -> isJaxRsBodyParam(pr.paramType)).findFirst();
    if (isNonBodyJaxRsMethod(d.httpMethod) && bodyParam.isPresent()) {
      throw new IllegalStateException(String.format("%s cannot define body parameter %s", bodyParam));
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
