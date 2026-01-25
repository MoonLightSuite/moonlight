// == Report aggregation plugin ==
// This plugin aggregates the test reports,
// regardless of the specific projects and languages
plugins {
    `kotlin-dsl`    // To compile the plugin code
    id("org.sonarqube") version "7.2.2.6593"
}

dependencies {
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:7.2.2.6593")
}
