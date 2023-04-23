plugins {
    id("eu.quanticol.java-library")
    kotlin("jvm") version "1.8.20"          // for compiling the docs
}

dependencies {
    implementation(project(":engine"))
    implementation("com.github.sh0nk:matplotlib4j:0.5.0")
}
repositories {
    mavenCentral()
}

