// == Javadoc website generation plugin ==
// This plugin generates the website with the docs
plugins {
    `kotlin-dsl`    // To compile the plugin code
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.8.20")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.20")
    dokkaPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.8.20")
}
