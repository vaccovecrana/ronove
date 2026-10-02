package io.vacco.ronove.plugin;

import io.github.classgraph.ClassGraph;
import io.vacco.ronove.reflect.RvContext;
import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class RvTask extends DefaultTask {

  private static final Logger log = Logging.getLogger(RvTask.class);

  private List<URL> getFilesFromConfiguration(String configuration) throws IOException {
    var urls = new ArrayList<URL>();
    for (var file : getProject().getConfigurations().getByName(configuration).getFiles()) {
      urls.add(file.toURI().toURL());
    }
    return urls;
  }

  private void write(File target, String content) throws IOException {
    var path = target.toPath();
    if (path.getParent() != null) {
      Files.createDirectories(path.getParent());
    }
    Files.write(path, content.getBytes(StandardCharsets.UTF_8));
  }

  private void generateGraalTypes(RvPluginExtension ext, RvContext ctx, RvTsContext tsx) {
    if (ext.reflectConfigFile.isPresent() || ext.reachabilityMetadataFile.isPresent()) {
      var graalTypes = new HashSet<Type>();
    }
  }

  private void doGenerate(Set<URL> urls) throws IOException {
    var ext = getProject().getExtensions().getByType(RvPluginExtension.class);
    var gradleCl = this.getClass().getClassLoader();
    try (var ucl = new URLClassLoader(urls.toArray(new URL[0]), gradleCl)) {
      var cg = new ClassGraph().verbose().enableAllInfo()
        .acceptClasses(ext.controllerClasses)
        .overrideClassLoaders(ucl);
      String tsSrc;
      try (var scanResult = cg.scan()) {
        var controllers = scanResult.getAllClasses().loadClasses();
        var ctx = new RvContext();
        var gen = new RvTsGen();
        var idx = ctx.describe(controllers);
        var tsx = RvTsContext.from(idx);
        tsSrc = gen.render(controllers, idx, tsx, ext.optionalFields);
        generateGraalTypes(ext, ctx, tsx);
      }
      write(ext.outFile.get().getAsFile(), tsSrc);
    }
  }

  @TaskAction
  public void action() {
    var urls = new LinkedHashSet<URL>();
    try {
      for (var task : getProject().getTasks()) {
        if (task.getName().startsWith("compile") && !task.getName().startsWith("compileTest")) {
          for (var file : task.getOutputs().getFiles()) {
            if (file.getAbsolutePath().contains("build/classes")) {
              urls.add(file.toURI().toURL());
            }
          }
        }
      }
      urls.addAll(getFilesFromConfiguration("compileClasspath"));
      urls.addAll(getFilesFromConfiguration("runtimeClasspath"));
      doGenerate(urls);
    } catch (Exception e) {
      var msg = "Unable to generate Typescript RCP definitions";
      log.error(msg, e);
      throw new IllegalStateException(msg, e);
    }
  }

}
