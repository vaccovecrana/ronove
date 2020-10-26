repositories { gradlePluginPortal() }

dependencies {
  api(gradleApi())
  api(project(":ronove-backend")) { exclude("io.vacco.shax", "shax") }
  implementation("cz.habarta.typescript-generator:typescript-generator-gradle-plugin:2.26.723")
  implementation("io.marioslab.basis:template:1.7")
}
