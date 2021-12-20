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
includeBuild("engine")
//includeBuild("utility")  // -> removed: moved into core

// == MoonlightScript ==
includeBuild("moonlightscript")

// == Moonlight APIs ==
includeBuild("api")
////includeBuild("console")  // -> removed: moved into api

// == Examples ==
includeBuild("examples")
