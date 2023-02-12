package io.vacco.ronove.plugin;

import cz.habarta.typescript.generator.*;
import cz.habarta.typescript.generator.gradle.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class RvPlugin implements Plugin<Project> {

  public static final String implementation = "implementation";

  @Override public void apply(Project project) {
    project.getDependencies().add(implementation, "jakarta.ws.rs:jakarta.ws.rs-api:3.0.0");
    project.getExtensions().create("ronove", RvPluginExtension.class, project);

    project.getPlugins().apply(TypeScriptGeneratorPlugin.class);
    project.getTasks().withType(GenerateTask.class).configureEach(gt -> {
      var j2 = new Jackson2Configuration();
      j2.enumsUsingToString = true;
      gt.jsonLibrary = JsonLibrary.jackson2;
      gt.jackson2Configuration = j2;
      gt.mapEnum = EnumMapping.asEnum;
      gt.outputFileType = TypeScriptFileType.implementationFile;
      gt.outputKind = TypeScriptOutputKind.module;
    });

    var classes = project.getTasks().getByName("classes");
    var generateTypeScript = project.getTasks().getByName("generateTypeScript");
    var rvTsRpc = project.getTasks().create("ronoveTypescriptRpc", RvTask.class);

    rvTsRpc.setGroup("build");
    rvTsRpc.setDescription("Generates TS schema and RPC call stubs for jax-rs annotated controllers");
    rvTsRpc.dependsOn(classes, generateTypeScript);
  }

}
