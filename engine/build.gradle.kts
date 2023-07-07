import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("io.github.moonlightsuite.java-library")
    id("io.github.moonlightsuite.generate-docs")
    id("io.github.moonlightsuite.publish")              // for publishing the JAR online
    id("com.vanniktech.maven.publish") version "0.25.2"
}

dependencies {
    compileOnly("org.jetbrains:annotations:23.0.0")
    testCompileOnly("org.jetbrains:annotations:23.0.0")

    implementation("com.google.code.gson:gson:2.10")
    implementation("org.apache.logging.log4j:log4j-core:2.17.2")
    implementation("org.apache.logging.log4j:log4j-api:2.17.2")

    testImplementation("org.mockito:mockito-core:4.8.0")
    testImplementation("org.jgrapht:jgrapht-core:1.5.1")
}


val PROJECT_VERSION = try {
    providers.gradleProperty("projectVersion").get()
} catch (e: Exception) {
    println("ERROR - Unable to find version: ${e.message}")
    "0.1.0-SNAPSHOT"
}

val PROJECT_GROUP = providers.gradleProperty("project.group").get()

mavenPublishing {
    coordinates(PROJECT_GROUP, rootProject.name, PROJECT_VERSION)

    publishToMavenCentral(SonatypeHost.S01, automaticRelease = true)

    signAllPublications()

    pom {
        name.set(rootProject.name)
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

afterEvaluate {
    tasks.named("generateMetadataFileForMavenPublication") {
        dependsOn("javaSourcesJar", "dokkaJavadocJar")
    }
}
