plugins { id("io.vacco.oss.gitflow") version "0.9.8" }

group = "io.vacco.ronove"
version = "0.2.0"

configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
  addJ8Spec()
  addClasspathHell()
  sharedLibrary(true, false)
}

configure<JavaPluginExtension> {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

val api by configurations

dependencies {
  implementation(gradleApi())
  implementation("cz.habarta.typescript-generator:typescript-generator-gradle-plugin:2.26.723")
  implementation("io.marioslab.basis:template:1.7")
  implementation("javax.ws.rs:javax.ws.rs-api:2.0")
  implementation("io.vacco.oruzka:oruzka:0.1.4")
}
