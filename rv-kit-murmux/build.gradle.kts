configure<io.vacco.oss.gitflow.GsPluginProfileExtension> { sharedLibrary(true, false) }

val api by configurations

dependencies {
  api(project(":rv-core"))
  implementation("io.vacco.murmux:murmux:2.2.0")
}
