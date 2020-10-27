pluginManagement {
  repositories {
    jcenter(); gradlePluginPortal()
    maven { name = "VaccoOss"; setUrl("https://dl.bintray.com/vaccovecrana/vacco-oss") }
  }
}

include("ronove-backend", "io.vacco.ronove.gradle.plugin")
