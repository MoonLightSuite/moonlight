plugins {
    id("eu.quanticol.java-library")
}

val libDir = "../../lib"

dependencies {
    implementation("eu.quanticol.moonlight:core")
    implementation("eu.quanticol.moonlight.api:matlab")
    implementation("eu.quanticol.moonlight:script")

    implementation(fileTree(mapOf("dir" to libDir, "include" to listOf("*.jar"))))
}
