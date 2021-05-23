dependencies {
  implementation(gradleApi())

  implementation("io.github.classgraph:classgraph:4.8.90")
  implementation("cz.habarta.typescript-generator:typescript-generator-core:2.26.723") { exclude("*","*") }
  implementation("cz.habarta.typescript-generator:typescript-generator-gradle-plugin:2.26.723") { exclude("*","*") }

  implementation("io.marioslab.basis:template:1.7")
  implementation("jakarta.ws.rs:jakarta.ws.rs-api:3.0.0")
  implementation("io.vacco.oruzka:oruzka:0.1.4")
}
