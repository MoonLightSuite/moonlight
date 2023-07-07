// == Main project tasks ==
// We collect here umbrella tasks that aggregate
// for convenience the tasks of the subprojects
plugins {
    id("io.github.moonlightsuite.java-library") // For java artifacts generation
    id("io.github.moonlightsuite.code-info") // for combining JaCoCo reports
    id("io.github.moonlightsuite.generate-docs") // For docs generation
}

// == Umbrella task to publishing all publishable packages ==
// TODO: ideally we should have separate packages:
//          1. api/console
//          2. engine/core
//          3. script
//          4. matlab
tasks.register<Copy>("release") {
    dependsOn("console:release")
}

// == Umbrella task to publish all ==
// TODO: still wip, for now publishes important stuff
tasks.register("publish") {
    dependsOn("engine:publish")
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
