package io.vacco.ronove.plugin;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

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
 */
public class RvGraalGen {

  private static final Logger log = Logging.getLogger(RvGraalGen.class);

  private static String entry(Class<?> c, String key, String indent) {
    return indent + "{\n" +
      indent + "  \"" + key + "\": \"" + c.getCanonicalName() + "\",\n" +
      indent + "  \"allDeclaredConstructors\": true,\n" +
      indent + "  \"allDeclaredFields\": true,\n" +
      indent + "  \"allDeclaredMethods\": true\n" +
      indent + "}";
  }

  public String reflectConfig(List<Class<?>> reflectTypes) {
    log.warn("Generating Graal reflect-config from {} types", reflectTypes.size());
    if (reflectTypes.isEmpty()) {
      return "[]\n";
    }
    var body = reflectTypes.stream()
      .map(c -> entry(c, "name", "  "))
      .collect(Collectors.joining(",\n"));
    return "[\n" + body + "\n]\n";
  }

  public String reachabilityMetadata(List<Class<?>> reflectTypes) {
    log.warn("Generating Graal reachability-metadata from {} types", reflectTypes.size());
    if (reflectTypes.isEmpty()) {
      return "{\n  \"reflection\": []\n}\n";
    }
    var body = reflectTypes.stream()
      .map(c -> entry(c, "type", "    "))
      .collect(Collectors.joining(",\n"));
    return "{\n  \"reflection\": [\n" + body + "\n  ]\n}\n";
  }

}
