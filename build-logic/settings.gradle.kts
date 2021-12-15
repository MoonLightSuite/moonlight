// The repositories from which we are fetching the libraries
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

// We aggregate all the build logic in a single project
rootProject.name = "build-logic"
include("java-library")
//include("report-aggregation")
