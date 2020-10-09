pluginManagement {
  repositories {
    jcenter(); gradlePluginPortal()
    maven { name = "VaccoOss"; setUrl("https://dl.bintray.com/vaccovecrana/vacco-oss") }
  }
}

include("ronove-backend", "ronove-gradle-plugin")
