// == Plugins for Java lifecycle ==
// This project defines plugins for:
// - testing dependencies,
// - java version, jvm settings
// - publishing maven packages
plugins {
    `kotlin-dsl`    // To compile the plugin code
    id("org.sonarqube") version "4.0.0.2929"
}

dependencies {
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:4.0.0.2929")
}
