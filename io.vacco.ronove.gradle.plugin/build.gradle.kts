repositories { gradlePluginPortal() }

dependencies {
  api(gradleApi())
  api(project(":ronove-backend")) { exclude("io.vacco.shax", "shax") }
  implementation("cz.habarta.typescript-generator:typescript-generator-gradle-plugin:2.26.723")
  implementation("io.marioslab.basis:template:1.7")
  implementation("io.vacco.oruzka:oruzka:0.1.0")
}

val versionNum = tasks.register("versionNumber") {
  doLast {
    File(project.buildDir, "resources/main/io/vacco/ronove/version")
        .writeText(project.version.toString())
  }
}

tasks.withType<Jar> { dependsOn(versionNum) }
