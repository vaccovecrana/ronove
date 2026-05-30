package io.vacco.ronove.plugin;

import io.marioslab.basis.template.TemplateContext;
import io.marioslab.basis.template.TemplateLoader;
import io.vacco.ronove.RvContext;
import io.vacco.ronove.RvDescriptor;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RvTsGen {

  private static final Logger log = Logging.getLogger(RvContext.class);

  public String render(List<Class<?>> controllers, boolean optionalFields) {
    log.warn("Generating RPC client from definitions: {}", controllers);
    var context = new TemplateContext();
    var loader = new TemplateLoader.ClasspathTemplateLoader();
    var template = loader.load("/io/vacco/ronove/codegen/rv-ts-rpc.bt");
    var ctx = new RvContext();
    var idx = ctx.describe(controllers);

    for (var rvd : idx.values()) {
      if (void.class.equals(rvd.responseType) || Void.class.equals(rvd.responseType)) {
        log.warn("RPC method [{}] returns void. This generates Promise<void> which may cause " +
          "runtime issues in TypeScript clients. Consider returning RvResponse<Void> instead.",
          rvd.javaMethod.getName());
      }
    }

    var tsCtx = new RvTsContext().add(
      idx.values().stream()
        .flatMap(RvDescriptor::allTypes)
        .collect(Collectors.toSet())
    );
    var tsTypes = tsCtx.schemaTypes();

    tsTypes.sort(Comparator.comparing(ts0 -> ts0.name));
    context.set("rvControllers", controllers.stream().map(Class::getCanonicalName).collect(Collectors.toList()));
    context.set("rvDescriptors", idx.values());
    context.set("tsSchemaTypes", tsTypes);
    context.set("retFn", (Function<Type, String>) RvTsDeclarations::mapReturn);
    context.set("paramFn", (Function<RvDescriptor, String>) RvTsDeclarations::mapParams);
    context.set("optionalFields", optionalFields);

    var out = template.render(context);
    out = Arrays.stream(out.split("\n"))
      .filter(line -> !"  ".equals(line))
      .filter(line -> !"    ".equals(line))
      .collect(Collectors.joining("\n"));

    return out;
  }

}
