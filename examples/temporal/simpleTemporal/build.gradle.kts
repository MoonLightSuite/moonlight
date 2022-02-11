plugins {
    id("eu.quanticol.java-library")
}

val libDir = "../../lib"

dependencies {
    implementation("eu.quanticol.moonlight:engine")
    implementation("eu.quanticol.moonlight:script")
}
