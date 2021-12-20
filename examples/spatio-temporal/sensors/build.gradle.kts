plugins {
    id("eu.quanticol.java-library")
}

val libDir = "../../lib"

dependencies {
    implementation("eu.quanticol.moonlight.engine:core")
    implementation("eu.quanticol.moonlight.engine:utility")
    implementation("eu.quanticol.moonlight:script")

    implementation(fileTree(mapOf("dir" to libDir, "include" to listOf("*.jar"))))
}
