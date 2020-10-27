package io.vacco.ronove.codegen;

import io.marioslab.basis.template.*;
import io.vacco.ronove.core.*;
import java.util.*;

import static java.util.Arrays.stream;

public class RvContext {

  public final Map<String, RvDescriptor> paths = new TreeMap<>();
  public final RvDescriptorFactory df = new RvDescriptorFactory();

  public Map<String, RvDescriptor> contextFor(List<Class<?>> controllers) {
    for (Class<?> ct : controllers) {
      stream(ct.getMethods())
          .filter(m -> stream(m.getAnnotations()).anyMatch(a -> RvOp.class.isAssignableFrom(a.getClass())))
          .forEach(m -> {
            RvDescriptor rd = df.describe(m);
            String path = rd.opMetadata.path();
            if (paths.containsKey(path)) {
              throw new RvException.RvDuplicateMappingException(path, rd, paths.get(path));
            }
            paths.put(path, rd);
          });
    }
    return paths;
  }

  public String map(List<Class<?>> controllers) {
    TemplateContext context = new TemplateContext();
    TemplateLoader loader = new TemplateLoader.ClasspathTemplateLoader();
    Template template = loader.load("/io/vacco/ronove/codegen/rv-ts-rpc.bt");
    context.set("rvDescriptors", contextFor(controllers).values());
    context.set("tsSchemaTypes", String.join(", ", df.tsSchemaTypes));
    return template.render(context);
  }

}
