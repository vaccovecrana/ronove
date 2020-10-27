package io.vacco.ronove;

import cz.habarta.typescript.generator.*;
import cz.habarta.typescript.generator.gradle.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class RvPlugin implements Plugin<Project> {

  @Override public void apply(Project project) {
    // TODO need to keep this dependency version in sync with the plugin in a better way.
    project.getDependencies().add("implementation", "io.vacco.ronove:ronove-backend:0.0.1");
    project.getExtensions().create("ronove", RvPluginExtension.class);
    project.getTasks().create("rvTypescriptRpc", RvTask.class);
    project.getPlugins().apply(TypeScriptGeneratorPlugin.class);

    project.getTasks().withType(GenerateTask.class).configureEach(gt -> {
      Jackson2Configuration j2 = new Jackson2Configuration();
      j2.enumsUsingToString = true;

      gt.jsonLibrary = JsonLibrary.jackson2;
      gt.jackson2Configuration = j2;
      gt.mapEnum = EnumMapping.asEnum;
      gt.outputFileType = TypeScriptFileType.implementationFile;
      gt.outputKind = TypeScriptOutputKind.module;
    });
  }
}
