plugins { `maven-publish`; `java-gradle-plugin` }

repositories { gradlePluginPortal() }

gradlePlugin {
  plugins {
    create("ronovePlugin") {
      id = "io.vacco.ronove"
      displayName = "Ronove"
      description = "Typescript frontend client generator."
      implementationClass = "io.vacco.ronove.RvPlugin"
    }
  }
}

dependencies {
  api(gradleApi())
  api(project(":ronove-backend")) { exclude("io.vacco.shax", "shax") }
  implementation("cz.habarta.typescript-generator:typescript-generator-gradle-plugin:2.26.723")
  implementation("io.marioslab.basis:template:1.7")
  implementation("io.vacco.oruzka:oruzka:0.1.0")
}
