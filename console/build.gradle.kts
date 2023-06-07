plugins {
    id("eu.quanticol.java-library")
    id("application") // plugin to add support for building a CLI application.
}

dependencies {
    implementation(project(":engine"))
    implementation(project(":script"))
}

application {
    // Define the main class for the application.
    mainClass.set("eu.quanticol.moonlight.MoonLightConsole")
    applicationName = "mlconsole"
}

fun copyAuxiliaryFiles(source: String, destination: String) {
    copy {
        from("$buildDir/resources/main/$source")
        into(rootProject.file("$rootDir/release/$destination"))
    }
}

fun copyFromTo(source: String, destination: String) =
        copy {
            println("Copying: ${file(source).listFiles()?.map{ it.name }} to $destination")
            from(file(source))
            into(rootProject.file(destination))
        }

tasks.register("distribution") {
    println("Executing :console:distribution.")
    dependsOn("jar")
    dependsOn("installDist")

    doLast {
        copyFromTo("$buildDir/libs/", "$rootDir/distribution_files/matlab/moonlight/jar/")
        copyFromTo("$buildDir/libs/", "$rootDir/distribution_files/console/")
    }
}

tasks.register("release") {
    println("Executing :console:release.")
    dependsOn("distribution")

    doLast {
        val source = rootProject.file("$rootDir/distribution_files/").path
        copyFromTo(source, "$rootDir/distribution/")
    }
}

tasks.named<Delete>("clean") {
    doFirst {
        delete("$rootDir/distribution/")
        println("jars in $rootDir/distribution/ deleted")
        delete("$rootDir/distribution_files/matlab/moonlight/jar/")
        println("jars in $rootDir/distribution_files/matlab/ deleted")
        delete("$rootDir/distribution_files/console/")
        println("jars in $rootDir/distribution_files/console/ deleted")
    }
}

