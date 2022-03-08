// == Report aggregation plugin ==
// This plugin aggregates the test reports,
// regardless of the specific projects and languages
plugins {
    `kotlin-dsl`    // To compile the plugin code
}

dependencies {
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:3.3")
}
