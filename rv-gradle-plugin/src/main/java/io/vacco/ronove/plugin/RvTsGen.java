package io.vacco.ronove.plugin;

import io.marioslab.basis.template.*;
import io.vacco.ronove.*;
import org.gradle.api.logging.*;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RvTsGen {

  private static final Logger log = Logging.getLogger(RvContext.class);

  public String render(List<Class<?>> controllers) {
    log.warn("Generating RPC client from definitions: {}", controllers);
    var context = new TemplateContext();
    var loader = new TemplateLoader.ClasspathTemplateLoader();
    var template = loader.load("/io/vacco/ronove/codegen/rv-ts-rpc.bt");
    var ctx = new RvContext();
    var idx = ctx.describe(controllers);

    var tsCtx = new RvTsContext().add(
        idx.values().stream()
            .flatMap(RvDescriptor::allTypes)
            .collect(Collectors.toSet())
    );
    var tsTypes = tsCtx.schemaTypes();

    context.set("rvControllers", controllers.stream().map(Class::getCanonicalName).collect(Collectors.toList()));
    context.set("rvDescriptors", idx.values());
    context.set("tsSchemaTypes", tsTypes);
    context.set("retFn", (Function<Type, String>) RvTsDeclarations::mapReturn);
    context.set("paramFn", (Function<RvDescriptor, String>) RvTsDeclarations::mapParams);

    return template.render(context);
  }

}
