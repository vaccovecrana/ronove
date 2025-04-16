package io.vacco.ronove.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class RvPlugin implements Plugin<Project> {

  public static final String implementation = "implementation";

  @Override public void apply(Project project) {
    project.getDependencies().add(implementation, "jakarta.ws.rs:jakarta.ws.rs-api:3.1.0");
    project.getExtensions().create("ronove", RvPluginExtension.class, project);

    var classes = project.getTasks().getByName("classes");
    var rvTsRpc = project.getTasks().register("ronoveTypescriptRpc", RvTask.class);

    rvTsRpc.configure(task -> {
      task.setGroup("build");
      task.setDescription("Generates TS schema and RPC call stubs for jax-rs annotated controllers");
      task.dependsOn(classes);
    });
  }

}
