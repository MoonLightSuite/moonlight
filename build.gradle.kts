// == Main project tasks ==
// We collect here umbrella tasks that aggregate
// for convenience the tasks of the subprojects
plugins {
    id("io.github.moonlightsuite.java-library") // For java artifacts generation
    id("io.github.moonlightsuite.code-info") // for combining JaCoCo reports
    id("io.github.moonlightsuite.generate-docs") // For docs generation
}

// == Umbrella task to publishing all publishable packages ==
tasks.register<Copy>("release") {
    dependsOn("console:release")
}

// == Umbrella task to publish all ==
// TODO: still wip, for now publishes important stuff
tasks.register("publishPackages") {
    dependsOn("engine:publish")
    dependsOn("script:publish")
}

tasks.register("analyze") {
    dependsOn("check")
    dependsOn("sonar")
}

dependencies {
    // Transitively collect coverage data from all features and their dependencies
    jacocoAggregation(project(":console"))
    jacocoAggregation(project(":engine"))
    jacocoAggregation(project(":script"))
    jacocoAggregation(project(":matlab"))

    // TODO: add examples, etc.
}

tasks.named<Copy>("copyDependencies") {
    copyModulesUpwards()
}

tasks.named<Copy>("copyJar") {
    copyModulesUpwards()
}

fun Copy.copyModulesUpwards() {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    subprojects.filter { it.name in listOf("engine", "script") }.forEach { project ->
        dependsOn(":${project.name}:$name")
        from("${project.buildDir}/jmods")
        into("$buildDir/jmods")
    }
}

// == Sonarqube settings ==
sonar {
    // TODO: change project key with `project.name` when sonarcloud is properly configured
    properties {
        property("sonar.projectKey", "MoonLightSuite_MoonLight")
        property("sonar.projectName", "Moonlight")
        property("sonar.organization", "moonlightsuite")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sourceEncoding", "UTF-8")

        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "./build/reports/jacoco/testCodeCoverageReport/testCodeCoverageReport.xml"
        )
    }
}
