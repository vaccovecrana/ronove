package io.vacco.ronove;

import io.marioslab.basis.template.*;
import io.vacco.ronove.core.*;
import org.gradle.api.logging.*;
import java.util.*;
import java.util.stream.Collectors;

public class RvTypescript {

  private static final Logger log = Logging.getLogger(RvContext.class);

  public String render(List<Class<?>> controllers) {
    log.warn("Generating RPC client from definitions: {}", controllers);
    TemplateContext context = new TemplateContext();
    TemplateLoader loader = new TemplateLoader.ClasspathTemplateLoader();
    Template template = loader.load("/io/vacco/ronove/codegen/rv-ts-rpc.bt");
    RvContext ctx = new RvContext();
    Map<String, RvDescriptor> idx = ctx.describe(controllers);

    context.set("rvControllers", controllers.stream().map(Class::getCanonicalName).collect(Collectors.toList()));
    context.set("rvDescriptors", idx.values());
    context.set("tsSchemaTypes", String.join(", ", ctx.tsFactory.tsSchemaTypes));

    return template.render(context);
  }

}
