// == Define locations for build logic ==
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    includeBuild("../build-logic")
}

// == Define the inner structure of this component ==
// Spatio-temporal examples:
include("spatio-temporal:city")
include("spatio-temporal:epidemic")
// temporal examples: