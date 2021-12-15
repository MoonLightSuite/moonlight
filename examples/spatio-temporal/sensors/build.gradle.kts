plugins {
    id("eu.quanticol.java-library")
}

val libDir = "../../lib"

dependencies {
    implementation("eu.quanticol.moonlight.core:monitor-core")
    implementation("eu.quanticol.moonlight.core:utility")
    implementation("eu.quanticol.moonlight.script:parser")

    implementation(fileTree(mapOf("dir" to libDir, "include" to listOf("*.jar"))))
}
