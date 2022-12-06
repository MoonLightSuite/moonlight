// == Main project tasks ==
// We collect here umbrella tasks that aggregate
// for convenience the tasks of the "sub"-projects
plugins {
    id("eu.quanticol.code-info")   // for combining JaCoCo reports
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

tasks.register("docs") {
    dependsOn(gradle.includedBuild("engine").task(":dokkaHtml"))
//    dependsOn(gradle.includedBuild("script").task(":dokkaHtmlMultiModule"))
}

// == Umbrella task to publish all ==
// TODO: still wip, for now publishes important stuff
tasks.register("publish") {
    dependsOn(gradle.includedBuild("engine").task(":publish"))
}

tasks.register("analyze") {
    dependsOn(tasks.named("check"))
    dependsOn(gradle.includedBuild("engine").task(":sonarqube"))
    //dependsOn(gradle.includedBuild("console").task(":sonarqube"))
    //dependsOn(gradle.includedBuild("script").task(":sonarqube"))
}



dependencies {
    // Transitively collect coverage data from all features and their dependencies
    jacocoAggregation("eu.quanticol.moonlight:console")
    jacocoAggregation("eu.quanticol.moonlight:engine")
    jacocoAggregation("eu.quanticol.moonlight:script")

    // TODO: add examples, etc.
}
