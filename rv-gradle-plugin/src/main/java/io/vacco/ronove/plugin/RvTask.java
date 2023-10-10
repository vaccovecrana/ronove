package io.vacco.ronove.plugin;

import io.github.classgraph.ClassGraph;
import org.gradle.api.*;
import org.gradle.api.logging.*;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class RvTask extends DefaultTask {

  private static final Logger log = Logging.getLogger(RvTask.class);

  private List<URL> getFilesFromConfiguration(String configuration) throws IOException {
    var urls = new ArrayList<URL>();
    for (File file : getProject().getConfigurations().getByName(configuration).getFiles()) {
      urls.add(file.toURI().toURL());
    }
    return urls;
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
        tsSrc = new RvTsGen().render(scanResult.getAllClasses().loadClasses());
      }
      Files.write(ext.outFile.get().getAsFile().toPath(), tsSrc.getBytes(StandardCharsets.UTF_8));
    }
  }

  @TaskAction public void action() {
    var urls = new LinkedHashSet<URL>();
    try {
      for (Task task : getProject().getTasks()) {
        if (task.getName().startsWith("compile") && !task.getName().startsWith("compileTest")) {
          for (File file : task.getOutputs().getFiles()) {
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
