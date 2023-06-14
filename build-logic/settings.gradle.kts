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
        `kotlin-dsl` apply false
        kotlin("jvm") version "1.8.10" apply false         // for compiling the docs
        id("org.jetbrains.dokka") version "1.8.20" apply false
    }
}

// == Define fetching locations for libraries and dependencies ==
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

// We aggregate all the build logic in a single project
include("java-library")
include("code-info")
include("generate-docs")
