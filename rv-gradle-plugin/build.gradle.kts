configure<io.vacco.oss.gitflow.GsPluginProfileExtension> { sharedLibrary(true, false) }

dependencies {
  implementation(gradleApi())
  implementation(project(":rv-core"))
  implementation("io.github.classgraph:classgraph:4.8.171")
  implementation("io.marioslab.basis:template:1.7")
}
