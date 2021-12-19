plugins {
    id("eu.quanticol.java-library")
}

val libDir = "../../lib"
val deps = listOf("engine2021.jar")

dependencies {
    implementation("eu.quanticol.moonlight.engine:core")
    implementation("eu.quanticol.moonlight.engine:utility")

    implementation(fileTree(mapOf("dir" to libDir, "include" to listOf("*.jar"))))
}
