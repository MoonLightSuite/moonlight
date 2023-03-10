// == Plugins for Java lifecycle ==
// This project defines plugins for:
// - testing dependencies,
// - java version, jvm settings
// - publishing maven packages
plugins {
    `kotlin-dsl`
    id("org.sonarqube") version "4.0.0.2929"
    id("org.jetbrains.dokka") version "1.8.10"
}

dependencies {
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:4.0.0.2929")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.8.10")
}
