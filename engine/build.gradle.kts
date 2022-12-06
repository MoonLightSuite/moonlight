plugins {
    id("eu.quanticol.java-library")
    id("eu.quanticol.publish")              // for publishing the JAR online
    kotlin("jvm") version "1.7.21"          // for compiling the docs
}

dependencies {
    implementation("com.google.code.gson:gson:2.10")
    implementation("org.jgrapht:jgrapht-core:1.5.1")
    implementation("org.jetbrains:annotations:23.0.0")
    implementation("com.github.sh0nk:matplotlib4j:0.5.0")
    implementation("org.apache.logging.log4j:log4j-core:2.17.2")
    implementation("org.apache.logging.log4j:log4j-api:2.17.2")
    testImplementation("org.mockito:mockito-core:4.8.0")
}
repositories {
    mavenCentral()
}

