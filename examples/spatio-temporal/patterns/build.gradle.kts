plugins {
    id("io.github.moonlightsuite.java-library")
}

val libDir = "../../lib"
val deps = listOf("engine2021.jar")

dependencies {
    implementation(project(":engine"))
    implementation(project(":matlab"))

    implementation(fileTree(mapOf("dir" to libDir, "include" to listOf("*.jar"))))
}
