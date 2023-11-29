// == Report aggregation plugin ==
// This plugin aggregates the test reports,
// regardless of the specific projects and languages
plugins {
    `kotlin-dsl`    // To compile the plugin code
    id("org.sonarqube") version "4.4.1.3373"
}

dependencies {
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:4.0.0.2929")
}
