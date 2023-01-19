// == Main project tasks ==
// We collect here umbrella tasks that aggregate
// for convenience the tasks of the "sub"-projects
plugins {
    // for combining JaCoCo reports:
    id("eu.quanticol.code-info")

    // for docs generation:
    kotlin("jvm")
    id("org.jetbrains.dokka")
}


// == Umbrella task to publishing all publishable packages ==
// TODO: ideally we should have three separate packages:
//          1. api/console
//          2. engine/core
//          3. script
tasks.register<Copy>("release") {
    dependsOn("console:release")
}

// == Umbrella task to clean all ==
// TODO: still wip, for now cleans important stuff
tasks.named("clean") {
    dependsOn("console:clean")
}

tasks.register("docs") {
    dependsOn(":dokkaHtmlMultiModule")
}


// == HTML javadoc settings ==
tasks.dokkaHtmlMultiModule.configure {
    outputDirectory.set(projectDir.resolve("docs"))
}

// == Umbrella task to publish all ==
// TODO: still wip, for now publishes important stuff
tasks.register("publish") {
    dependsOn("engine:publish")
}

tasks.register("analyze") {
    dependsOn(tasks.named("check"))
    dependsOn("engine:sonarqube")
    dependsOn("script:sonarqube")
    //dependsOn(gradle.includedBuild("console").task(":sonarqube"))
    //dependsOn(gradle.includedBuild("script").task(":sonarqube"))
}



dependencies {
    // Transitively collect coverage data from all features and their dependencies
    jacocoAggregation(project(":console"))
    jacocoAggregation(project(":engine"))
    jacocoAggregation(project(":script"))

    // TODO: add examples, etc.
}
