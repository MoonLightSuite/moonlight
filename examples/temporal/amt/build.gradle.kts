plugins {
    id("io.github.moonlightsuite.java-library")
}

val libDir = "../../lib"

dependencies {
    implementation(project(":engine"))

    /* GAMT Dependency */
    implementation(fileTree(mapOf("dir" to libDir, "include" to "gamt.jar")))
}
