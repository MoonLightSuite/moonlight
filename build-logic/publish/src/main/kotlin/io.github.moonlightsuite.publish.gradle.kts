import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `java-library`
//    id("io.github.moonlightsuite.java-library")
    id("com.vanniktech.maven.publish")
}

// == General Java settings ==
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
//    withJavadocJar()
//    withSourcesJar()
}

val projectVersion: String = try {
    val ver = providers.gradleProperty("projectVersion").get()
    if (ver.contains("-")) {
        println("WARNING: publishing snapshot version")
        "${ver.split("-")[0]}-SNAPSHOT"
    } else ver
} catch (e: IllegalStateException) {
    println("ERROR - Unable to find version: ${e.message}")
    "0.1.0-SNAPSHOT"
}

val projectGroup: String = providers.gradleProperty("project.group").get()

mavenPublishing {
    coordinates(projectGroup, "${rootProject.name}-${project.name}", projectVersion)

    publishToMavenCentral(SonatypeHost.S01, automaticRelease = true)

    signAllPublications()

    pom {
        name.set("${rootProject.name}-${project.name}")
        description.set("MoonLight is a light-weight Java-tool for monitoring temporal, spatial and spatio-temporal properties of distributed complex systems, such as Cyber-Physical Systems and Collective Adaptive Systems.")
        url.set("https://github.com/moonlightsuite/moonlight")
        licenses {
            license {
                name.set("Apache License 2.0")
                url.set("https://raw.githubusercontent.com/MoonLightSuite/MoonLight/master/LICENSE")
            }
        }
        developers {
            developer {
                id.set("michele-loreti")
                name.set("Michele Loreti")
                email.set("michele.loreti@unicam.it")
            }
            developer {
                id.set("lauranenzi")
                name.set("Laura Nenzi")
                email.set("lnenzi@units.it")
            }
            developer {
                id.set("ennioVisco")
                name.set("Ennio Visconti")
                email.set("ennio.visconti@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/moonlightsuite/moonlight.git")
            developerConnection.set("scm:git:ssh://github.com/moonlightsuite/moonlight.git")
            url.set("https://github.com/moonlightsuite/moonlight")
        }
    }
}

tasks.withType<GenerateModuleMetadata> {
    dependsOn("dokkaJavadocJar")
}
