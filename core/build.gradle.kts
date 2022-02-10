plugins {
    id("eu.quanticol.java-library")
    id("eu.quanticol.publish")              // for publishing the JAR online
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("org.jgrapht:jgrapht-core:1.4.0")
    implementation("org.jetbrains:annotations:23.0.0")
    implementation("com.github.sh0nk:matplotlib4j:0.5.0")
    implementation("org.apache.logging.log4j:log4j-core:2.17.1")
    implementation("org.apache.logging.log4j:log4j-api:2.17.1")
}
