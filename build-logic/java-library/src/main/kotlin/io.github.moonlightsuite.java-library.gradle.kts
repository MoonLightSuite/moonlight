plugins {
    `java-library`
    java
    jacoco
}

group = "io.github.moonlightsuite.moonlight"
version = "1.0-SNAPSHOT"
description = "MoonLight: a light-weight framework for runtime monitoring"

// == General Java settings ==
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
//    withJavadocJar()
//    withSourcesJar()
}

tasks {
    // to allow UTF-8 characters in comments
    compileJava { options.encoding = "UTF-8" }
    compileTestJava { options.encoding = "UTF-8" }
}

// == Testing settings ==
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks.test {
    useJUnitPlatform()
}


tasks.jacocoTestReport.configure {
    // Do not generate reports for individual projects
    enabled = false
}

tasks.register<Copy>("copyDependencies") {
    from(configurations.runtimeClasspath).into(layout.buildDirectory.dir("jmods"))
}

tasks.register<Copy>("copyJar") {
    from(tasks.jar).into(layout.buildDirectory.dir("jmods"))
}
