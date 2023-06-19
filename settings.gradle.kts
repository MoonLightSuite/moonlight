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
categories.forEach { category ->
    File("examples/$category").listFiles()?.forEach { example ->
        if(example.isDirectory) {
            include("examples:$category:${example.name}")
        }
    }
}
