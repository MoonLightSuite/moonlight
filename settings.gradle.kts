// == Main project ==
// This is an empty umbrella project including all the component builds.
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
    includeBuild("build-logic") // this project contains the advanced gradle scripts
}

// We include all the "sub"-projects in the build process:

// == Moonlight Core ==
include("engine")
//includeBuild("utility")  // -> removed: moved into api

// == MoonlightScript ==
include("script")

// == Plotting ==
include("plotting")

// == Python ==
include("python")

// == Legacy Moonlight APIs for Matlab == // TODO: refactor
include("matlab")

// == CLI project ==
include("console")

// == Examples ==
val categories = listOf("temporal", "spatio-temporal")
val examples = categories.map { category ->
    File("examples/$category").listFiles()?.map {
        example -> "examples:$category:${example.name}"
    } ?: listOf()
}.flatten()
include(examples)

//include("examples:temporal:matlab-simple")
