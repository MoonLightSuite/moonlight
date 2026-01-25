plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    dokkaPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.9.20")
}

tasks.register("docs") {
    dependsOn(":dokkaHtmlMultiModule")
}

// == HTML javadoc settings ==
tasks.withType<org.jetbrains.dokka.gradle.DokkaMultiModuleTask> {
    outputDirectory.set(rootDir.resolve("docs"))
}
