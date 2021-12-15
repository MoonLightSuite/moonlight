plugins {
    id("java-library")
    id("application") // plugin to add support for building a CLI application.
}

dependencies {
    implementation("eu.quanticol.moonlight.core:monitor-core")
    //implementation(project(":api"))
    //implementation(project(":moonlightscript"))
}

application {
    // Define the main class for the application.
    mainClass.set("eu.quanticol.moonlight.MoonLightConsole")
    applicationName = "mlconsole"
}

tasks.jar {
    archiveFileName.set("moonlight.jar")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    manifest {
        attributes(mapOf("Main-Class" to application.mainClass))
    }
    doFirst {
        from({
            configurations.compileClasspath.get().map {
                if (it.isDirectory) it else zipTree(it)
            }
            //duplicatesStrategy(DuplicatesStrategy.WARN)
        })
    }
    exclude ("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}

val DEPRECATED = "Dangerous copying strategy adopted for legacy code, should be refactored. " +
                 "See https://docs.gradle.org/current/userguide/upgrading_version_5.html#implicit_duplicate_strategy_for_copy_or_archive_tasks_has_been_deprecated"

tasks.named("installDist") {
    logger.warn(DEPRECATED)
    //duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
tasks.named("distTar") {
    logger.warn(DEPRECATED)
    //duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
tasks.named("distZip") {
    logger.warn(DEPRECATED)
    //duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.register<Copy>("distribution") {
    logger.info("exec distribution Task!")
    dependsOn("jar")
    dependsOn("installDist")

    logger.warn(DEPRECATED)
    //duplicatesStrategy(DuplicatesStrategy.INCLUDE)

    copy {
        from("jar")
        into(rootProject.file("distribution_files/java/lib/"))
        into(rootProject.file("distribution_files/matlab/moonlight/jar"))
        into(rootProject.file("distribution_files/python/jar"))
        into(rootProject.file("distribution_files/console/lib/"))
    }

    copy {
        from("$buildDir/install/mlconsole/")
        into(rootProject.file("distribution_files/console/"))
    }

}

//tasks.withType<Copy> {
//    duplicatesStrategy = DuplicatesStrategy.INCLUDE
//}

tasks.register<Copy>("release") {
    dependsOn("distribution")

    copy {
        from(rootProject.file("distribution_files/"))
        into(rootProject.file("distribution/"))
    }
}

//task distributionZip(type: Zip, dependsOn: distribution) {
//    archiveFileName = "moonlight.zip"
//    destinationDirectory = file("$rootDir/distribution/")
//    from "$rootDir/distribution/"
//}