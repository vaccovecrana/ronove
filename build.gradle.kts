plugins { id("io.vacco.common-build") version "0.5.3" }

subprojects {
  apply(plugin = "io.vacco.common-build")
  group = "io.vacco.ronove"
  version = "0.1.0"

  configure<io.vacco.common.CbPluginProfileExtension> {
    addJ8Spec()
    addPmd()
    addSpotBugs()
    sharedLibrary()
    setPublishingUrlTransform { repo -> "${repo.url}/${rootProject.name}" }
  }

  configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
}
