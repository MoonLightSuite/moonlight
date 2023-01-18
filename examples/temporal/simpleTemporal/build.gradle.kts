plugins {
    id("eu.quanticol.java-library")
}

val libDir = "../../lib"

dependencies {
    implementation(project(":engine"))
    implementation(project(":script"))
}
