plugins {
    id("eu.quanticol.java-library")
}

group = "${group}.engine"

dependencies {
    implementation(project(":core"))
    //implementation fileTree(dir: "lib", include: "engine.jar")
    //implementation fileTree(dir: "lib", include: "engine2019.jar")
    implementation(fileTree(mapOf("dir" to "lib", "include" to "engine2021.jar")))
    implementation("org.n52.matlab:matlab-control:5.0.0")
}