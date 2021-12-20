// == Repository plugin collection ==
// We collect in this project all the plugins that
// will simplify tasks for the moonlight suite.

// == Define fetching locations for libraries and dependencies ==
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

// == Define the inner structure of this component ==
rootProject.name = "build-logic"    // the component name
// We aggregate all the build logic in a single project
include("java-library")
include("report-aggregation")
