plugins {
    id("io.github.moonlightsuite.java-library")
    id("io.github.moonlightsuite.generate-docs")
    id("io.github.moonlightsuite.publish")
}

dependencies {
    compileOnly("org.jetbrains:annotations:23.0.0")
    testCompileOnly("org.jetbrains:annotations:23.0.0")

    implementation("com.google.code.gson:gson:2.10")
    implementation("org.apache.logging.log4j:log4j-core:2.17.2")
    implementation("org.apache.logging.log4j:log4j-api:2.17.2")

    testImplementation("org.mockito:mockito-core:4.8.0")
    testImplementation("org.jgrapht:jgrapht-core:1.5.1")
}
