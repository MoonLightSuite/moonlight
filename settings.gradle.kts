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

// We include all the "sub"-projects in the build process:

// == Common scripts ==
includeBuild("build-logic")

// == Moonlight Core ==
includeBuild("core")
//includeBuild("utility")  // -> removed: moved into api

// == MoonlightScript ==
includeBuild("moonlightscript")

// == Legacy Moonlight APIs for Matlab == // TODO: refactor
includeBuild("api")

// == CLI project ==
includeBuild("console")

// == Examples ==
includeBuild("examples")
