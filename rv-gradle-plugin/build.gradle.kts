dependencies {
  implementation(gradleApi())
  implementation(project(":rv-core"))

  implementation("io.github.classgraph:classgraph:4.8.90")
  implementation("cz.habarta.typescript-generator:typescript-generator-core:2.26.723") { exclude("*","*") }
  implementation("cz.habarta.typescript-generator:typescript-generator-gradle-plugin:2.26.723") { exclude("*","*") }

  implementation("io.marioslab.basis:template:1.7")
  implementation("io.vacco.oruzka:oruzka:0.1.4")
}
