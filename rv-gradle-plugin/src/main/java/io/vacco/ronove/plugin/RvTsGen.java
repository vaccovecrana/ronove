package io.vacco.ronove.plugin;

import io.marioslab.basis.template.*;
import io.vacco.ronove.RvContext;
import io.vacco.ronove.RvDescriptor;
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

    context.set("rvControllers", controllers.stream().map(Class::getCanonicalName).collect(Collectors.toList()));
    context.set("rvDescriptors", idx.values());
    context.set("tsSchemaTypes", tsCtx.schemaTypes());
    context.set("decl", (Function<Type, String>) RvTsDeclarations::mapTail);
    context.set("declList", (Function<RvDescriptor, String>) RvTsDeclarations::mapParams);

    return template.render(context);
  }

}
