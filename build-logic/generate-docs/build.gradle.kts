// == Javadoc website generation plugin ==
// This plugin generates the website with the docs
plugins {
    `kotlin-dsl`    // To compile the plugin code
    id("org.jetbrains.dokka")
}

dependencies {
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:2.1.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21")
}
