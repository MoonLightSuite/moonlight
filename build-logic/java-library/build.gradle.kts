// == Plugins for Java lifecycle ==
// This project defines plugins for:
// - testing dependencies,
// - java version, jvm settings
// - publishing maven packages
plugins {
    `kotlin-dsl`    // To compile the plugin code
    id("com.vanniktech.maven.publish") version "0.25.3"
}

dependencies {
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.25.3")
}
