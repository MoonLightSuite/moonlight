plugins {
    id("eu.quanticol.java-library")
    id("eu.quanticol.publish")              // for publishing the JAR online
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("org.jgrapht:jgrapht-core:1.4.0")
}