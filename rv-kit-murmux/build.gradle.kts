configure<io.vacco.oss.gitflow.GsPluginProfileExtension> { sharedLibrary(true, false) }

val api by configurations
val murmuxVer = "2.2.2"
version = murmuxVer

dependencies {
  api(project(":rv-core"))
  api("io.vacco.murmux:murmux:${murmuxVer}")
}
