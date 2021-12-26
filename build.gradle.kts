// == Main project tasks ==
// We collect here umbrella tasks that aggregate
// for convenience the tasks of the "sub"-projects
plugins {
    id("eu.quanticol.report-aggregation")   // for combining JaCoCo reports
    id("eu.quanticol.publish")              // for publishing the JAR online
}

// TODO: unclear whether still needed
//subprojects {
//    ext.xtextVersion = "2.18.0.M3"
//}

// == Umbrella task to publishing all publishable packages ==
// TODO: ideally we should have three separate packages:
//          1. api/console
//          2. engine
//          3. script
tasks.register<Copy>("release") {
    dependsOn(gradle.includedBuild("api").task(":console:release"))
}

// == Umbrella task to clean all ==
// TODO: still wip, for now cleans important stuff
tasks.named("clean") {
    dependsOn(gradle.includedBuild("api").task(":console:clean"))
}


dependencies {
    // Transitively collect coverage data from all features and their dependencies
    aggregate("eu.quanticol.moonlight.engine:core")
    aggregate("eu.quanticol.moonlight.engine:utility")
    aggregate("eu.quanticol.moonlight:script")
    // TODO: add examples, etc.
}