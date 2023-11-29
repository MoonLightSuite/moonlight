plugins {
    kotlin("jvm")               // for compiling the docs
    id("org.jetbrains.dokka")
}

dependencies {
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.9.10")
    dokkaPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.9.10")
}

tasks.register("docs") {
    dependsOn(":dokkaHtmlMultiModule")
}

// == HTML javadoc settings ==
tasks.withType<org.jetbrains.dokka.gradle.DokkaMultiModuleTask> {
    outputDirectory.set(rootDir.resolve("docs"))

}

tasks.named("docs") {
    dependsOn(":dokkaHtmlMultiModule")
}
