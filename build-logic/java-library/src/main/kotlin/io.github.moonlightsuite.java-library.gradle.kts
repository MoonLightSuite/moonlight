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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
//    withJavadocJar()
//    withSourcesJar()
}

tasks.withType<JavaCompile> {
    // Needed by pattern matching on switches:
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<Javadoc> {
    // Needed by pattern matching on switches:
    val javadocOptions = options as CoreJavadocOptions
    javadocOptions.addStringOption("source", "17")
    javadocOptions.addBooleanOption("-enable-preview", true)
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
    // Needed by pattern matching on switches:
    jvmArgs("--enable-preview")
}


tasks.jacocoTestReport.configure {
    // Do not generate reports for individual projects
    enabled = false
}

tasks.register<Copy>("copyDependencies") {
    from(configurations.runtimeClasspath).into("$buildDir/jmods")
}

tasks.register<Copy>("copyJar") {
    from(tasks.jar).into("$buildDir/jmods")
}
