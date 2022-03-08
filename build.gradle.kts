// == Main project tasks ==
// We collect here umbrella tasks that aggregate
// for convenience the tasks of the "sub"-projects
plugins {
    id("eu.quanticol.report-aggregation")   // for combining JaCoCo reports
    id("org.sonarqube") version "3.3"
}

// == Umbrella task to publishing all publishable packages ==
// TODO: ideally we should have three separate packages:
//          1. api/console
//          2. engine/core
//          3. script
tasks.register<Copy>("release") {
    dependsOn(gradle.includedBuild("console").task(":release"))
}

// == Umbrella task to clean all ==
// TODO: still wip, for now cleans important stuff
tasks.named("clean") {
    dependsOn(gradle.includedBuild("console").task(":clean"))
}

// == Umbrella task to publish all ==
// TODO: still wip, for now publishes important stuff
//tasks.register("publish") {
//    dependsOn(gradle.includedBuild("core").task(":publish"))
//}



sonarqube {
    properties {
        property("sonar.projectKey", "MoonLightSuite_MoonLight")
        property("sonar.organization", "moonlightsuite")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}


dependencies {
    // Transitively collect coverage data from all features and their dependencies
    jacocoAggregation("eu.quanticol.moonlight:console")
    jacocoAggregation("eu.quanticol.moonlight:core")
    jacocoAggregation("eu.quanticol.moonlight:script")
    // TODO: add examples, etc.
}