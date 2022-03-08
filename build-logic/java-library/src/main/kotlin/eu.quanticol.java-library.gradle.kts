plugins {
    `java-library`
    id("eu.quanticol.jacoco")
}

group = "eu.quanticol.moonlight"
version = "1.0-SNAPSHOT"
description = "MoonLight: a light-weight framework for runtime monitoring"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    // to allow UTF-8 characters in comments
    compileJava { options.encoding = "UTF-8" }
    compileTestJava { options.encoding = "UTF-8" }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

tasks.test {
    useJUnitPlatform()
}
