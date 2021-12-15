// == Define locations for build logic ==
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    includeBuild("../build-logic")
}

// == Define the inner structure of this component ==
rootProject.name = "script"
include("parser")
