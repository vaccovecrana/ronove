package io.vacco.ronove.codegen;

import io.marioslab.basis.template.*;
import io.vacco.ronove.core.*;
import org.gradle.api.logging.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class RvContext {

  private static final Logger log = Logging.getLogger(RvContext.class);

  public final Map<String, RvDescriptor> paths = new TreeMap<>();
  public final RvDescriptorFactory df = new RvDescriptorFactory();

  public Map<String, RvDescriptor> contextFor(List<Class<?>> controllers) {
    for (Class<?> ct : controllers) {
      for (Method m : ct.getMethods()) {
        log.warn(m.toString());
        for (Annotation an : m.getAnnotations()) {
          if (RvOp.class.isAssignableFrom(an.getClass())) {
            RvDescriptor rd = df.describe(m);
            String path = rd.opMetadata.path();
            if (paths.containsKey(path)) {
              throw new RvException.RvDuplicateMappingException(path, rd, paths.get(path));
            }
            paths.put(path, rd);
          }
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
    context.set("rvDescriptors", contextFor(controllers).values());
    context.set("tsSchemaTypes", String.join(", ", df.tsSchemaTypes));
    return template.render(context);
  }

}
