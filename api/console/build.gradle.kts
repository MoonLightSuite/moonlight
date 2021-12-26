plugins {
    id("eu.quanticol.java-library")
    id("application") // plugin to add support for building a CLI application.
}

group = "${group}.api"


dependencies {
    implementation("eu.quanticol.moonlight.engine:core")
    implementation("eu.quanticol.moonlight:script")
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
        })
    }
    exclude ("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}

//val DEPRECATED = "Dangerous copying strategy adopted for legacy code, should be refactored. " +
//                 "See https://docs.gradle.org/current/userguide/upgrading_version_5.html#implicit_duplicate_strategy_for_copy_or_archive_tasks_has_been_deprecated"
//
//tasks.matching{it.name in listOf("installDist", "distTar", "distZip")}
//    .configureEach {
//    logger.warn(DEPRECATED)
//    //duplicatesStrategy = DuplicatesStrategy.INCLUDE
//}

val moonlightJar: CopySpec = copySpec {
    from("$buildDir/libs/")
}

tasks.register("distribution") {
    logger.info("exec distribution Task!")
    dependsOn("jar")
    dependsOn("installDist")

    doFirst {
        copy {
            from("$buildDir/libs/")
            into(rootProject.file("$rootDir/../distribution_files/java/lib/"))
        }

        copy {
            from("$buildDir/libs/")
            into(rootProject.file("$rootDir/../distribution_files/matlab/moonlight/jar/"))
        }

        copy {
            from("$buildDir/libs/")
            into(rootProject.file("$rootDir/../distribution_files/python/jar/"))
        }

        copy {
            from("$buildDir/install/mlconsole/")
            into(rootProject.file("$rootDir/../distribution_files/console/"))
        }
    }
}

//  home/runner/work/MoonLight/MoonLight/ api/../distribution/java/lib
// /home/runner/work/MoonLight/MoonLight/ distribution/java/lib/moonlight.jar

tasks.register<Copy>("release") {
    println("Executing :console:release.")
    dependsOn("distribution")

    from(rootProject.file("$rootDir/../distribution_files/"))
    into(rootProject.file("$rootDir/../distribution/"))
    logger.info("jar saved in $rootDir/../distribution/java/lib")
    val files = file("$rootDir/../distribution/java/lib").listFiles().map{ it.name }
    logger.info("Folders: ${files.toString()}")
}

tasks.named<Delete>("clean") {
    doFirst {
        delete("$rootDir/../distribution/")
        logger.info("jars in $rootDir/../distribution/ deleted")
        delete("$rootDir/../distribution_files/java/lib/moonlight.jar")
        logger.info("jars in $rootDir/../distribution_files/java/ deleted")
        delete("$rootDir/../distribution_files/matlab/moonlight/jar/")
        logger.info("jars in $rootDir/../distribution_files/matlab/ deleted")
        delete("$rootDir/../distribution_files/python/jar/")
        logger.info("jars in $rootDir/../distribution_files/python/ deleted")
        delete("$rootDir/../distribution_files/console/")
        logger.info("jars in $rootDir/../distribution_files/console/ deleted")
    }
}

