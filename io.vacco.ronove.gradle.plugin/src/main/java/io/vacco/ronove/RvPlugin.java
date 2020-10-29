package io.vacco.ronove;

import cz.habarta.typescript.generator.*;
import cz.habarta.typescript.generator.gradle.*;
import io.vacco.oruzka.io.OzIo;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class RvPlugin implements Plugin<Project> {

  @Override public void apply(Project project) {
    String rvVersion = OzIo.loadFrom(getClass().getResource("/io/vacco/ronove/version"));
    String rvBackend = String.format("io.vacco.ronove:ronove-backend:%s", rvVersion);

    project.getDependencies().add("implementation", rvBackend);
    project.getExtensions().create("ronove", RvPluginExtension.class, project);
    project.getTasks().create("ronoveTypescriptRpc", RvTask.class);
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
