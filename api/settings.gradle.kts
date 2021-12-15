// == Define locations for build logic ==
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    includeBuild("../build-logic")
}

// == Define the inner structure of this component ==
include("matlab")
include("console")
