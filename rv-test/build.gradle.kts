configure<io.vacco.oss.gitflow.GsPluginProfileExtension> { sharedLibrary(false, false) }

tasks.withType<JacocoReport> {
  sourceSets(
    project(":rv-core").sourceSets.main.get(),
    project(":rv-kit-undertow").sourceSets.main.get(),
    project(":rv-kit-murmux").sourceSets.main.get(),
    project(":io.vacco.ronove.gradle.plugin").sourceSets.main.get()
  )
}

dependencies {
  implementation(project(":rv-core"))
  implementation(project(":rv-kit-undertow"))
  implementation(project(":rv-kit-murmux"))
  implementation(project(":io.vacco.ronove.gradle.plugin"))
  implementation("io.vacco.oruzka:oruzka:0.1.5.1")

  testImplementation("com.google.code.gson:gson:2.10.1")
  testImplementation("com.github.mizosoft.methanol:methanol:1.7.0")
  testImplementation("org.slf4j:jul-to-slf4j:2.0.6")
  testImplementation("io.undertow:undertow-core:2.3.3.Final")
  testImplementation("io.vacco.murmux:murmux:2.2.0")
}