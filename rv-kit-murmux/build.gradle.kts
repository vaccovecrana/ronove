configure<io.vacco.oss.gitflow.GsPluginProfileExtension> { sharedLibrary(true, false) }

val api by configurations
val murmuxVer = "[2,)"
val mxp = project(":rv-core")

version = "${mxp.version}_${murmuxVer}"

dependencies {
  api(project(":rv-core"))
  api("io.vacco.murmux:murmux:${murmuxVer}")
}
