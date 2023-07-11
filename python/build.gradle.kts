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
        println("From: $buildDir/libs/${project.name}-all.jar")
        println("To: $projectDir/src/moonlight/jar")
        from("$buildDir/libs/${project.name}-all.jar")
        into("$projectDir/src/moonlight/jar")
    }
}

tasks.create<Delete>("clearArtifacts") {
    project.delete("$projectDir/src/moonlight/jar")
    project.delete("$buildDir/libs/")
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
