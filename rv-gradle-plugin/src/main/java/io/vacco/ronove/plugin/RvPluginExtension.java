package io.vacco.ronove.plugin;

import org.gradle.api.Project;
import org.gradle.api.file.RegularFileProperty;

public class RvPluginExtension {

  public RegularFileProperty outFile;
  public String[] controllerClasses = new String[]{};
  public boolean optionalFields = false;

  public RvPluginExtension(Project p) {
    this.outFile = p.getObjects().fileProperty();
  }

}
