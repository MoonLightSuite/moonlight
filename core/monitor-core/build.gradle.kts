plugins {
    id("eu.quanticol.java-library")
}

//group = "${group}.core"
println(">>>>>>>>>>>>>>${group} : ${rootProject.name}");

dependencies {
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("org.jgrapht:jgrapht-core:1.5.1")
}

//jar {
//    archiveFileName= "moonlight.jar"
//}