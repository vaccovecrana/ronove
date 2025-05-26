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

  testImplementation("com.google.code.gson:gson:[2,)")
  testImplementation("com.github.mizosoft.methanol:methanol:[1,)")
  testImplementation("org.slf4j:jul-to-slf4j:2.0.16")
  testImplementation("io.vacco.shax:shax:[2.0.16,)")
}