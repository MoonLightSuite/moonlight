plugins {
    id("org.jetbrains.dokka")
}

tasks.register("docs") {
    dependsOn(":dokkaGenerate")
}
