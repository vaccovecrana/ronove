val api by configurations

dependencies {
  implementation(gradleApi())
  implementation("cz.habarta.typescript-generator:typescript-generator-gradle-plugin:2.26.723")
  implementation("io.marioslab.basis:template:1.7")
  implementation("javax.ws.rs:javax.ws.rs-api:2.0")
  implementation("io.vacco.oruzka:oruzka:0.1.4")
}
