plugins {
    id("eu.quanticol.java-library")
}

group = "${group}.core"

dependencies {
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("org.jgrapht:jgrapht-core:1.4.0")
}

//jar {
//    archiveFileName= "moonlight.jar"
//}