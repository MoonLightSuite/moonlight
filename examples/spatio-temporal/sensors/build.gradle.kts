plugins {
    id("io.github.moonlightsuite.java-library")
}

val libDir = "../../lib"

dependencies {
    implementation(project(":engine"))
    implementation(project(":matlab"))
    implementation(project(":script"))
    implementation(project(":plotting"))

    implementation(fileTree(mapOf("dir" to libDir, "include" to listOf("*.jar"))))
}
