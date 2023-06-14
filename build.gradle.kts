// == Main project tasks ==
// We collect here umbrella tasks that aggregate
// for convenience the tasks of the subprojects
plugins {
    id("eu.quanticol.code-info") // for combining JaCoCo reports

    // for docs generation:
    kotlin("jvm")
    id("org.jetbrains.dokka")

    id("eu.quanticol.java-library") // For java artifacts generation
}

// == Umbrella task to publishing all publishable packages ==
// TODO: ideally we should have three separate packages:
//          1. api/console
//          2. engine/core
//          3. script
tasks.register<Copy>("release") {
    dependsOn("console:release")
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
    dependsOn("check")
    dependsOn("sonar")
//    dependsOn("engine:sonarqube")
//    dependsOn("script:sonarqube")
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
