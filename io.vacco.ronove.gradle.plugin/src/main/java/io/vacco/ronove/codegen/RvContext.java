package io.vacco.ronove.codegen;

import io.marioslab.basis.template.*;
import io.vacco.ronove.core.*;
import org.gradle.api.logging.*;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class RvContext {

  private static final Logger log = Logging.getLogger(RvContext.class);

  public final Map<String, RvDescriptor> paths = new TreeMap<>();
  public final RvDescriptorFactory df = new RvDescriptorFactory();

  public RvDescriptor describe(Method m, Path p, Annotation jaxRsMethod) {
    log.warn("Method: ({}) {} {}", jaxRsMethod, p, m);
    RvDescriptor d = new RvDescriptor();
    d.path = p;
    d.opMethod = m;
    d.httpMethod = jaxRsMethod.toString().replace("@javax.ws.rs.", "").replace("()", "");
    d.tsOutputType = df.tsArgsOf((ParameterizedType) m.getGenericReturnType());
    if (m.getParameters().length == 1) {
      Type t = m.getParameters()[0].getParameterizedType();
      d.tsInputType = t instanceof ParameterizedType ? df.tsArgsOf((ParameterizedType) t) : df.tsTypeOf(t);
    }
    return d;
  }

  private boolean isJaxRsMethod(Annotation an) {
    Class<? extends Annotation> anc = an.getClass();
    return GET.class.isAssignableFrom(anc)
        || POST.class.isAssignableFrom(anc)
        || PUT.class.isAssignableFrom(anc)
        || DELETE.class.isAssignableFrom(anc)
        || PATCH.class.isAssignableFrom(anc)
        || HEAD.class.isAssignableFrom(anc)
        || OPTIONS.class.isAssignableFrom(anc);
  }

  public Map<String, RvDescriptor> contextFor(List<Class<?>> controllers) {
    for (Class<?> ct : controllers) {
      for (Method m : ct.getMethods()) {
        Optional<Annotation> op = Arrays.stream(m.getAnnotations()).filter(an -> Path.class.isAssignableFrom(an.getClass())).findFirst();
        Optional<Annotation> ojxm = Arrays.stream(m.getAnnotations()).filter(this::isJaxRsMethod).findFirst();
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

  public String map(List<Class<?>> controllers) {
    log.warn("Generating RPC client from definitions: {}", controllers);
    TemplateContext context = new TemplateContext();
    TemplateLoader loader = new TemplateLoader.ClasspathTemplateLoader();
    Template template = loader.load("/io/vacco/ronove/codegen/rv-ts-rpc.bt");
    context.set("rvControllers", controllers.stream().map(Class::getCanonicalName).collect(Collectors.toList()));
    context.set("rvDescriptors", contextFor(controllers).values());
    context.set("tsSchemaTypes", String.join(", ", df.tsSchemaTypes));
    return template.render(context);
  }

}
