package io.vacco.ronove.plugin;

import io.vacco.ronove.reflect.RvContext;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Renders GraalVM native-image reflection metadata from the resolved type graph,
 * so JSON (de)serialization of DTOs works inside native images without having
 * to run the tracing agent.
 *
 * <p>Two output formats are supported: the classic {@code reflect-config.json}
 * (GraalVM 22+, still read by 23.x/24.x), and the newer
 * {@code reachability-metadata.json} introduced in GraalVM 23.1.</p>
 *
 * <p>Controller classes are included alongside the DTO type graph. Ronove
 * discovers handler methods at runtime via {@code Class.getMethods()} and
 * {@code Method.getAnnotations()}, which native-image strips unless the
 * controller itself is registered for reflection. This is required for
 * controllers using {@code @BeanParam} (or any parameter annotation), whose
 * method annotations must remain queryable at runtime.</p>
 *
 * <p>Every entry also sets {@code unsafeAllocated}, since gson instantiates
 * request/response DTOs through {@code Unsafe.allocateInstance}.</p>
 */
public class RvGraalGen {

  private static final Logger log = Logging.getLogger(RvGraalGen.class);

  private static String entry(Type t, String key, String indent) {
    return indent + "{\n" +
      indent + "  \"" + key + "\": \"" + t.getTypeName() + "\",\n" +
      indent + "  \"allDeclaredConstructors\": true,\n" +
      indent + "  \"allDeclaredFields\": true,\n" +
      indent + "  \"allDeclaredMethods\": true,\n" +
      indent + "  \"unsafeAllocated\": true\n" +
      indent + "}";
  }

  public String reflectConfig(List<Type> types) {
    log.warn("Generating Graal reflect-config from {} types", types.size());
    if (types.isEmpty()) {
      return "[]\n";
    }
    var body = types.stream()
      .map(c -> entry(c, "name", "  "))
      .collect(Collectors.joining(",\n"));
    return "[\n" + body + "\n]\n";
  }

  public String reachabilityMetadata(List<Type> types) {
    log.warn("Generating Graal reachability-metadata from {} types", types.size());
    if (types.isEmpty()) {
      return "{\n  \"reflection\": []\n}\n";
    }
    var body = types.stream()
      .map(c -> entry(c, "type", "    "))
      .collect(Collectors.joining(",\n"));
    return "{\n  \"reflection\": [\n" + body + "\n  ]\n}\n";
  }

  public void render(RvContext ctx, File rcJsonOutFile, File rmJsonOutFile) {
    var types = ctx.metadataTypes();
    if (rcJsonOutFile != null) {
      RvTask.write(reflectConfig(types), rcJsonOutFile);
    }
    if (rmJsonOutFile != null) {
      RvTask.write(reachabilityMetadata(types), rmJsonOutFile);
    }
  }

}
