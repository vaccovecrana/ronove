configure<io.vacco.oss.gitflow.GsPluginProfileExtension> { sharedLibrary(true, false) }

val api by configurations
val undertowVer = "2.3.8.Final"
version = undertowVer

dependencies {
  api(project(":rv-core"))
  api("io.undertow:undertow-core:${undertowVer}")
}
