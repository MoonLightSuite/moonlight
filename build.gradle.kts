plugins {
    id("eu.quanticol.report-aggregation")
    id("eu.quanticol.publish")
}

// TODO: unclear whether still needed
//subprojects {
//    ext.xtextVersion = "2.18.0.M3"
//}

tasks.register<Copy>("release") {
    dependsOn(gradle.includedBuild("api").task(":console:release"))
}

tasks.named("clean") {
    dependsOn(gradle.includedBuild("api").task(":console:clean"))
}


dependencies {
    // Transitively collect coverage data from all features and their dependencies
    aggregate("eu.quanticol.moonlight.engine:core")
    aggregate("eu.quanticol.moonlight.engine:utility")
//    aggregate("eu.quanticol.moonlight:examples")
}