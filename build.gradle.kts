plugins { id("io.vacco.oss.gitflow") version "1.0.1" }

subprojects {
  apply(plugin = "io.vacco.oss.gitflow")
  group = "io.vacco.ronove"
  version = "1.3.0"

  configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
    addJ8Spec()
    addClasspathHell()
  }

  configure<io.vacco.cphell.ChPluginExtension> {
    resourceExclusions.add("module-info.class")
  }
}
