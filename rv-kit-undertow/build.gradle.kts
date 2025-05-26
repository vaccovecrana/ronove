configure<io.vacco.oss.gitflow.GsPluginProfileExtension> { sharedLibrary(true, false) }

val api by configurations

dependencies {
  api(project(":rv-core"))
  api("io.undertow:undertow-core:[2,)")
}
