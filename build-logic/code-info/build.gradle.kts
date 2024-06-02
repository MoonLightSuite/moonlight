// == Report aggregation plugin ==
// This plugin aggregates the test reports,
// regardless of the specific projects and languages
plugins {
    `kotlin-dsl`    // To compile the plugin code
    id("org.sonarqube") version "5.0.0.4638"
}

dependencies {
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:5.0.0.4638")
}
