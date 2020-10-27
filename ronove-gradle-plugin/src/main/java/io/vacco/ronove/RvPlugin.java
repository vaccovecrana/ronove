package io.vacco.ronove;

import cz.habarta.typescript.generator.gradle.TypeScriptGeneratorPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class RvPlugin implements Plugin<Project> {

  @Override public void apply(Project project) {
    // TODO need to keep this dependency version in sync with the plugin in a better way.
    project.getDependencies().add("implementation", "io.vacco.ronove:ronove-backend:0.0.1");
    project.getExtensions().create("ronove", RvPluginExtension.class);
    project.getTasks().create("rvTypescriptRpc", RvTask.class);
    project.getPlugins().apply(TypeScriptGeneratorPlugin.class);
  }
}
