package io.vacco.ronove;

import cz.habarta.typescript.generator.*;
import cz.habarta.typescript.generator.gradle.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class RvPlugin implements Plugin<Project> {

  public static final String implementation = "implementation";

  @Override public void apply(Project project) {
    // project.getDependencies().add(implementation, "javax.ws.rs:javax.ws.rs-api:2.1.1");
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
