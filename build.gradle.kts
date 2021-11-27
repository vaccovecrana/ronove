plugins { id("io.vacco.oss.gitflow") version "0.9.8" }

subprojects {
  apply(plugin = "io.vacco.oss.gitflow")
  group = "io.vacco.ronove"
  version = "0.1.3"

  configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
    addJ8Spec()
    addClasspathHell()
    sharedLibrary(true, false)
  }
}
