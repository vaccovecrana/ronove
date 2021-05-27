package io.vacco.ronove;

import io.github.classgraph.*;
import io.vacco.oruzka.core.OFnBlock;
import io.vacco.ronove.core.RvContext;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class RvTask extends DefaultTask {

  private List<URL> getFilesFromConfiguration(String configuration) throws IOException {
    List<URL> urls = new ArrayList<>();
    for (File file : getProject().getConfigurations().getByName(configuration).getFiles()) {
      urls.add(file.toURI().toURL());
    }
    return urls;
  }

  private void doGenerate(Set<URL> urls) throws IOException {
    RvPluginExtension ext = getProject().getExtensions().getByType(RvPluginExtension.class);
    ClassLoader gradleCl = this.getClass().getClassLoader();
    try (URLClassLoader ucl = new URLClassLoader(urls.toArray(new URL[0]), gradleCl)) {
      ClassGraph cg = new ClassGraph().verbose().enableAllInfo()
          .acceptClasses(ext.controllerClasses)
          .overrideClassLoaders(ucl);
      String tsSrc;
      try (ScanResult scanResult = cg.scan()) {
        tsSrc = new RvTypescript().render(scanResult.getAllClasses().loadClasses());
      }
      Files.write(ext.outFile.get().getAsFile().toPath(), tsSrc.getBytes(StandardCharsets.UTF_8));
    }
  }

  @TaskAction public void action() {
    Set<URL> urls = new LinkedHashSet<>();
    OFnBlock.tryRun(() -> {
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
    });
  }

}
