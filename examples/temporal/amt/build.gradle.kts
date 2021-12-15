plugins {
    id("eu.quanticol.java-library")
}

val libDir = "../../lib"

dependencies {
    implementation("eu.quanticol.moonlight.core:monitor-core")

    /* GAMT Dependency */
    implementation(fileTree(mapOf("dir" to libDir, "include" to "gamt.jar")))
}
