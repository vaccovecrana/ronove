plugins { `java-library`; id("io.vacco.common-build") }

configure<io.vacco.common.CbPluginProfileExtension> { sharedLibrary() }
dependencies { api("org.codejargon.feather:feather:1.0") }
