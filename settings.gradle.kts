pluginManagement { repositories { mavenCentral(); gradlePluginPortal() } }

include("rv-gradle-plugin", "rv-kit-undertow")

project(":rv-gradle-plugin").name = "io.vacco.ronove.gradle.plugin"
