plugins {
    id("eu.quanticol.java-library")
}

group = "${group}.api"


dependencies {
    implementation("eu.quanticol.moonlight.engine:core")
    implementation("eu.quanticol.moonlight.engine:utility")
    implementation("eu.quanticol.moonlight:script")
}
//tasks.jar {
//    archiveFileName= "moonlightAPI.jar"
//}