// == Plugins for Java lifecycle ==
// This project defines plugins for:
// - testing dependencies,
// - java version, jvm settings
// - publishing maven packages
plugins {
    `kotlin-dsl`    // To compile the plugin code
    id("org.sonarqube") version "4.3.0.3225"
    id("com.vanniktech.maven.publish") version "0.25.3"
}

dependencies {
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:4.0.0.2929")
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.25.3")
}
