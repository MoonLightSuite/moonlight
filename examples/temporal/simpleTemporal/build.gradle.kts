plugins {
    id("io.github.moonlightsuite.java-library")
}

val libDir = "../../lib"

dependencies {
    implementation(project(":engine"))
    implementation(project(":script"))
}
