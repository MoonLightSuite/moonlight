// == Repository plugin collection ==
// We collect in this project all the plugins that
// will simplify tasks for the moonlight suite.
// == Define locations for build logic ==
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("org.jetbrains.dokka") version "1.9.10" apply false
    }
}

// == Define fetching locations for libraries and dependencies ==
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

// Required to remove annoying warning in subprojects
plugins {
    `kotlin-dsl` apply false
}

// We aggregate all the build logic in a single project
include("java-library")
include("publish")
include("code-info")
include("generate-docs")
