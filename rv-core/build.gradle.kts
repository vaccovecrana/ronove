configure<io.vacco.oss.gitflow.GsPluginProfileExtension> { sharedLibrary(true, false) }

val api by configurations

dependencies {
  api("jakarta.ws.rs:jakarta.ws.rs-api:3.0.0")
}