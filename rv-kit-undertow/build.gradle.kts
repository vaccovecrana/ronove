configure<io.vacco.oss.gitflow.GsPluginProfileExtension> { sharedLibrary(true, false) }

val api by configurations
val undertowVer = "2.3.18.Final"
val mxp = project(":rv-core")

version = "${mxp.version}_${undertowVer}"

dependencies {
  api(mxp)
  api("io.undertow:undertow-core:${undertowVer}")
}
