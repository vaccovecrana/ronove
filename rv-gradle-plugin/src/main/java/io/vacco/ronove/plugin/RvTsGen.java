package io.vacco.ronove.plugin;

import io.marioslab.basis.template.TemplateContext;
import io.marioslab.basis.template.TemplateLoader;
import io.vacco.ronove.reflect.RvContext;
import io.vacco.ronove.reflect.RvMethod;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RvTsGen {

  private static final Logger log = Logging.getLogger(RvContext.class);

  public void render(RvContext ctx, boolean optionalFields, File tsSrcOutFile) {
    log.warn("Generating RPC client from definitions: {}", ctx.controllers);
    var context = new TemplateContext();
    var loader = new TemplateLoader.ClasspathTemplateLoader();
    var template = loader.load("/io/vacco/ronove/codegen/rv-ts-rpc.bt");

    for (var rvd : ctx.paths.values()) {
      if (void.class.equals(rvd.responseType.from) || Void.class.equals(rvd.responseType.from)) {
        log.warn("RPC method [{}] returns void. This generates Promise<void> which may cause " +
          "runtime issues in TypeScript clients. Consider returning RvResponse<Void> instead.",
          rvd.javaMethod.getName());
      }
    }

    var types = ctx.schemaTypes();
    context.set("rvControllers", ctx.controllers.stream().map(ct -> ct.clazz).map(Class::getCanonicalName).collect(Collectors.toList()));
    context.set("rvDescriptors", ctx.paths.values());
    context.set("tsSchemaTypes", types);
    context.set("retFn", (Function<Type, String>) ctx.typeCache::nameReturn);
    context.set("nameGenericRaw", (Function <Type, String>) ctx.typeCache::nameGenericRaw);
    context.set("paramFn", (Function<RvMethod, String>) ctx.typeCache::nameParams);
    context.set("optionalFields", optionalFields);

    var src = template.render(context);
    src = Arrays.stream(src.split("\n"))
      .filter(line -> !"  ".equals(line))
      .filter(line -> !"    ".equals(line))
      .collect(Collectors.joining("\n"));

    RvTask.write(src, tsSrcOutFile);
  }

}
