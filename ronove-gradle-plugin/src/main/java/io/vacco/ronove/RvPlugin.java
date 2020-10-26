package io.vacco.ronove;

import cz.habarta.typescript.generator.gradle.TypeScriptGeneratorPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class RvPlugin implements Plugin<Project> {

  @Override public void apply(Project project) {
    project.getExtensions().create("ronove", RvPluginExtension.class);
    project.getTasks().create("rvTypescriptRpc", RvTask.class);
    project.getPlugins().apply(TypeScriptGeneratorPlugin.class);
  }
}
