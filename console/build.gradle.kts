plugins {
    id("io.github.moonlightsuite.java-library")
    id("application") // plugin to add support for building a CLI application.
}

dependencies {
    implementation(project(":engine"))
    implementation(project(":script"))
}

application {
    // Define the main class for the application.
    mainClass.set("io.github.moonlightsuite.moonlight.MoonLightConsole")
    applicationName = "mlconsole"
}

fun copyAuxiliaryFiles(source: String, destination: String) {
    copy {
        from(layout.buildDirectory.file("resources/main/$source"))
        into(rootProject.layout.buildDirectory.file("$rootDir/release/$destination"))
    }
}

fun copyFromTo(source: Provider<Directory>, destination: Provider<Directory>) =
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
        val libs = layout.buildDirectory.dir("libs")
        val moonlightJar = rootProject.layout.buildDirectory.dir("distribution_files/matlab/moonlight/jar")
        val console = rootProject.layout.buildDirectory.dir("distribution_files/console")
        copyFromTo(libs, moonlightJar)
        copyFromTo(libs, console)
    }
}

tasks.register("release") {
    println("Executing :console:release.")
    dependsOn("distribution")

    doLast {
        val distributionFiles = rootProject.layout.buildDirectory.dir("distribution_files")
        val distribution = rootProject.layout.buildDirectory.dir("distribution_files")
        copyFromTo(distributionFiles, distribution)
    }
}

tasks.named<Delete>("clean") {
    dependsOn("removeFiles")
}

tasks.register("removeFiles") {
    delete("$rootDir/distribution/")
    println("jars in $rootDir/distribution/ deleted")
    delete("$rootDir/distribution_files/matlab/moonlight/jar/")
    println("jars in $rootDir/distribution_files/matlab/ deleted")
    delete("$rootDir/distribution_files/console/")
    println("jars in $rootDir/distribution_files/console/ deleted")
}

