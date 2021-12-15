// The repositories from which we are fetching the libraries
dependencyResolutionManagement {
    //repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        //gradlePluginPortal()
    }
}

// We aggregate all the build logic in a single project
rootProject.name = "build-logic"
include("java-library")
//include("report-aggregation")
