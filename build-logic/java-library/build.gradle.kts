// == Plugins for Java lifecycle ==
// This project defines plugins for:
// - testing dependencies,
// - java version, jvm settings
// - publishing maven packages
plugins {
    `kotlin-dsl`
    id("org.sonarqube") version "3.3"
    id("org.jetbrains.dokka") version "1.7.20"
}

dependencies {
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:3.3")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.7.20")
}
