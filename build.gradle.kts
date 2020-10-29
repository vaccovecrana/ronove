plugins { id("io.vacco.common-build") version "0.5.3" }

subprojects {
  apply(plugin = "io.vacco.common-build")
  group = "io.vacco.ronove"
  version = "0.1.1"

  configure<io.vacco.common.CbPluginProfileExtension> {
    addJ8Spec()
    addPmd()
    addSpotBugs()
    setPublishingUrlTransform { repo -> "${repo.url}/${rootProject.name}" }
    sharedLibrary()
  }

  configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
}
