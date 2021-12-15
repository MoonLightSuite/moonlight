plugins {
    id("eu.quanticol.java-library")
}

dependencies {
    implementation(project(":monitor-core"))
    //implementation fileTree(dir: "lib", include: "engine.jar")
    //implementation fileTree(dir: "lib", include: "engine2019.jar")
    implementation(fileTree(mapOf("dir" to "lib", "include" to "engine2021.jar")))
    //implementation group: "org.n52.matlab", name: "matlab-control", version: "5.0.0"
    testImplementation("org.n52.matlab:matlab-control:5.0.0")
}