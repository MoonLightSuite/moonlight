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
            //duplicatesStrategy(DuplicatesStrategy.WARN)
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

tasks.register<Copy>("distribution") {
    logger.info("exec distribution Task!")
    dependsOn("jar")
    dependsOn("installDist")

    //logger.warn(DEPRECATED)
    //duplicatesStrategy(DuplicatesStrategy.INCLUDE)

    copy {
        from("jar")
        into(rootProject.file("../distribution_files/java/lib/"))
        into(rootProject.file("../distribution_files/matlab/moonlight/jar"))
        into(rootProject.file("../distribution_files/python/jar"))
        into(rootProject.file("../distribution_files/console/lib/"))
    }

    copy {
        from("$buildDir/install/mlconsole/")
        into(rootProject.file("../distribution_files/console/"))
    }

}

tasks.register<Copy>("release") {
    dependsOn("distribution")

    copy {
        from(rootProject.file("../distribution_files/"))
        into(rootProject.file("../distribution/"))
    }
}

tasks.named<Delete>("clean") {
    doFirst {
        delete ("${rootDir}/../output/")
        println ("deleting jars in ${rootDir}/../output/")
        delete ("${rootDir}/../distribution/")
        println ("deleting jars in ${rootDir}/../distribution/")
        delete ("${rootDir}/../distribution_files/java/lib/moonlight.jar")
        println ("deleting ${rootDir}/../distribution_files/java/lib/moonlight.jar")
        delete ("${rootDir}/../distribution_files/matlab/moonlight/jar/")
        println ("deleting jars in ${rootDir}/../distribution_files/matlab/moonlight/jar/")
        delete ("${rootDir}/../distribution_files/python/jar/")
        println ("${rootDir}/../distribution_files/python/jar/")
    }
}

