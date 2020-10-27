package io.vacco.ronove;

import io.github.classgraph.*;
import io.vacco.oruzka.core.OFnBlock;
import io.vacco.ronove.codegen.RvContext;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class RvTask extends DefaultTask {

  @TaskAction public void action() {
    RvContext ctx = new RvContext();
    RvPluginExtension ext = getProject().getExtensions().getByType(RvPluginExtension.class);
    ClassGraph cg = new ClassGraph().verbose().enableAllInfo().acceptPackages(ext.schemaPackages);
    String tsSrc;

    try (ScanResult scanResult = cg.scan()) {
      tsSrc = ctx.map(scanResult.getAllClasses().loadClasses());
    }

    OFnBlock.tryRun(
        () -> Files.write(ext.outFile.get().getAsFile().toPath(), tsSrc.getBytes(StandardCharsets.UTF_8))
    );
  }

}
