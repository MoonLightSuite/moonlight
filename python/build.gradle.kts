plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    implementation(project(":engine"))
    implementation(project(":script"))
}


fun updateExecutableJar() {
    copy {
        val buildJar = layout.buildDirectory.file("libs/${project.name}-all.jar")
        val projectJarDir = layout.projectDirectory.dir("src/moonlight/jar")
        println("From: $buildJar")
        println("To: $projectJarDir")
        from(buildJar)
        into(projectJarDir)
    }
}

tasks.create<Delete>("clearArtifacts") {
    project.delete(layout.projectDirectory.dir("src/moonlight/jar"))
    project.delete(layout.buildDirectory.dir("libs"))
    project.delete("dist")
}

tasks.clean {
    dependsOn("clearArtifacts")
}

tasks.shadowJar {
    dependsOn("clearArtifacts")
}

tasks.register("distribute") {
    dependsOn("shadowJar")

    doLast {
        updateExecutableJar()
    }
}
