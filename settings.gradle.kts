pluginManagement { repositories { mavenCentral(); gradlePluginPortal() } }

include(
  "rv-core", "rv-gradle-plugin",
  "rv-kit-undertow", "rv-kit-murmux",
  "rv-test"
)

project(":rv-gradle-plugin").name = "io.vacco.ronove.gradle.plugin"

rootProject.name = "ronove"
