plugins {
    id("io.github.moonlightsuite.java-library")
    id("io.github.moonlightsuite.generate-docs")
}

// Retrocompatibility, as Matlab only supports up to Java 11.
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    // Needed by pattern matching on switches:
    options.compilerArgs.remove("--enable-preview")
}

dependencies {
    //implementation fileTree(dir: "lib", include: "engine.jar")
    //implementation fileTree(dir: "lib", include: "engine2019.jar")
    implementation(fileTree(mapOf("dir" to "lib", "include" to "engine2021.jar"))) // Matlab 2021 engine
    implementation("org.n52.matlab:matlab-control:5.0.0")
}
