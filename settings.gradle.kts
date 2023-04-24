// == Main project ==
// This is an empty umbrella build including all the component builds.
// This build is not necessarily needed: component builds work independently.
// == Main project's name ==
rootProject.name = "moonlight" // the component name

// == Define locations for components ==
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

pluginManagement {
    includeBuild("build-logic")
    plugins {
        kotlin("jvm") version "1.8.20" apply false         // for compiling the docs
        id("org.jetbrains.dokka") version "1.8.10" apply false
    }
}


// We include all the "sub"-projects in the build process:

// == Common scripts ==
//include("build-logic")

// == Moonlight Core ==
include("engine")
//includeBuild("utility")  // -> removed: moved into api

// == MoonlightScript ==
include("script")

// == Plotting ==
include("plotting")

// == Legacy Moonlight APIs for Matlab == // TODO: refactor
include("matlab")

// == CLI project ==
include("console")

// == Examples ==
include("examples")
