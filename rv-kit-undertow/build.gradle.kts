configure<io.vacco.oss.gitflow.GsPluginProfileExtension> { sharedLibrary(true, false) }

val api by configurations

dependencies {
  api(project(":rv-core"))
  implementation("io.undertow:undertow-core:2.3.3.Final")
}
