plugins {
    id("eu.quanticol.java-library")
}

val libDir = "../../lib"

dependencies {
    implementation(project(":engine"))
    implementation(project(":matlab"))
    implementation(project(":script"))

    implementation(fileTree(mapOf("dir" to libDir, "include" to listOf("*.jar"))))
}
