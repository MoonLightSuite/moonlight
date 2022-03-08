plugins {
    id("eu.quanticol.java-library")
    id("maven-publish")
}

publishing {
    publications {
        // We define a Maven Package for publication
        create<MavenPublication>("mavenJava") {
            artifactId = rootProject.name
            groupId = "$group"
            version = "1.0-SNAPSHOT"
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }

        }
    }
    repositories {
        // We define the repository for GitHub Packages
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/MoonlightSuite/MoonLight")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}