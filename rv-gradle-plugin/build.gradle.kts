configure<io.vacco.oss.gitflow.GsPluginProfileExtension> { sharedLibrary(true, false) }

dependencies {
  implementation(gradleApi())
  implementation(project(":rv-core"))
  implementation("cz.habarta.typescript-generator:typescript-generator-gradle-plugin:3.1.1185")
  implementation("io.marioslab.basis:template:1.7")
}

configurations.all {
  exclude("javax.xml.bind", "jaxb-api")
  exclude("javax.activation", "javax.activation-api")
}