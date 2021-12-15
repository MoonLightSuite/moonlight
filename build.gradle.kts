plugins {
    id("eu.quanticol.report-aggregation")
    id("eu.quanticol.publish")
}
//group = "eu.quanticol.moonlight"
//allprojects {
//    //repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
//    repositories {
//        mavenCentral()
//        gradlePluginPortal()
//    }
//}

//tasks.register<GradleBuild>("build") {
//    tasks = listOf("clean", "build", "publish")
//}

tasks.register("exp") {
    println("Building moonlight")
    subprojects {
        println("Project name ${this.name}")
        println(this.tasks.forEach { e -> println(e)})
    }
}

tasks.register<Copy>("release") {
    dependsOn(gradle.includedBuild("api").task(":console:release"))
}

tasks.named("clean") {
    dependsOn(gradle.includedBuild("api").task(":console:clean"))
}


dependencies {
    // Trasitively collect coverage data from all features and their dependencies
    aggregate("eu.quanticol.moonlight.core:monitor-core")
    aggregate("eu.quanticol.moonlight:examples")
}