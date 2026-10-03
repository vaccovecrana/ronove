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

  public static void write(String content, File target) {
    try {
      var path = target.toPath();
      if (path.getParent() != null) {
        Files.createDirectories(path.getParent());
      }
      Files.writeString(path, content);
    } catch (Exception e) {
      throw new IllegalStateException(String.format(
        "Unable to write file: %s", target.getAbsolutePath()
      ), e);
    }
  }

  private void doGenerate(Set<URL> urls) throws IOException {
    var ext = getProject().getExtensions().getByType(RvPluginExtension.class);
    var gradleCl = this.getClass().getClassLoader();
    try (var ucl = new URLClassLoader(urls.toArray(new URL[0]), gradleCl)) {
      var cg = new ClassGraph().verbose().enableAllInfo()
        .acceptClasses(ext.controllerClasses)
        .overrideClassLoaders(ucl);
      var tsg = new RvTsGen();
      var grg = new RvGraalGen();
      var ctx = new RvContext();
      try (var scanResult = cg.scan()) {
        var controllers = scanResult.getAllClasses().loadClasses();
        ctx.describe(controllers);
      }
      tsg.render(ctx, ext.optionalFields, ext.outFile.get().getAsFile());
      if (ext.reflectConfigFile.isPresent() || ext.reachabilityMetadataFile.isPresent()) {
        grg.render(
          ctx,
          ext.reflectConfigFile.isPresent() ? ext.reflectConfigFile.get().getAsFile() : null,
          ext.reachabilityMetadataFile.isPresent() ? ext.reachabilityMetadataFile.get().getAsFile() : null
        );
      }
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
