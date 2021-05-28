configure<io.vacco.oss.gitflow.GsPluginProfileExtension> { sharedLibrary(true, false) }

val api by configurations

dependencies {
  api(project(":rv-core"))
  api("io.vacco.flatbread:flatbread:0.1.1")
  api("org.codejargon.feather:feather:1.0")
  api("com.google.code.gson:gson:2.8.6")
  api("io.undertow:undertow-core:2.2.7.Final")
}
