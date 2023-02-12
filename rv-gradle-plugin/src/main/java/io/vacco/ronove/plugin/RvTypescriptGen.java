package io.vacco.ronove.plugin;

import io.marioslab.basis.template.*;
import io.vacco.ronove.RvContext;
import org.gradle.api.logging.*;
import java.util.*;
import java.util.stream.Collectors;

public class RvTypescriptGen {

  private static final Logger log = Logging.getLogger(RvContext.class);

  public String render(List<Class<?>> controllers) {
    log.warn("Generating RPC client from definitions: {}", controllers);
    var context = new TemplateContext();
    var loader = new TemplateLoader.ClasspathTemplateLoader();
    var template = loader.load("/io/vacco/ronove/codegen/rv-ts-rpc.bt");
    var ctx = new RvContext();
    var idx = ctx.describe(controllers);

    context.set("rvControllers", controllers.stream().map(Class::getCanonicalName).collect(Collectors.toList()));
    context.set("rvDescriptors", idx.values());
    context.set("tsSchemaTypes", String.join(", ", ctx.tsFactory.tsSchemaTypes));

    return template.render(context);
  }

}
