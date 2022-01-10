// == Main project tasks ==
// We collect here umbrella tasks that aggregate
// for convenience the tasks of the "sub"-projects
plugins {
    id("eu.quanticol.report-aggregation")   // for combining JaCoCo reports
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
    dependsOn(gradle.includedBuild("console").task(":release"))
}

// == Umbrella task to clean all ==
// TODO: still wip, for now cleans important stuff
tasks.named("clean") {
    dependsOn(gradle.includedBuild("console").task(":clean"))
}

// == Umbrella task to publish all ==
// TODO: still wip, for now cleans important stuff
tasks.register("publish") {
    dependsOn(gradle.includedBuild("core").task(":publish"))
}


dependencies {
    // Transitively collect coverage data from all features and their dependencies
    aggregate("eu.quanticol.moonlight:core")
    aggregate("eu.quanticol.moonlight:script")
    aggregate("eu.quanticol.moonlight:console")
    aggregate("eu.quanticol.moonlight.api:matlab")
    // TODO: add examples, etc.
}