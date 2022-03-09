plugins {
    `java-library`
    id("eu.quanticol.jacoco")
    id("org.sonarqube")
}

group = "eu.quanticol.moonlight"
version = "1.0-SNAPSHOT"
description = "MoonLight: a light-weight framework for runtime monitoring"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    // to allow UTF-8 characters in comments
    compileJava { options.encoding = "UTF-8" }
    compileTestJava { options.encoding = "UTF-8" }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

tasks.test {
    useJUnitPlatform()
}

sonarqube {
    properties {
        property("sonar.projectKey", "MoonLightSuite_MoonLight")
        property("sonar.organization", "moonlightsuite")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sourceEncoding", "UTF-8")

        //property("sonar.coverage.jacoco.xmlReportPaths", "../build/reports/jacoco/testCodeCoverageReport/testCodeCoverageReport.xml")

//        //TODO: these properties are workarounds to pass multi-project sources to sonarqube
//        property("sonar.java.sources", "core/src/main/java")
//        property("sonar.java.tests", "core/src/test/java")
//        property("sonar.java.binaries", "core/build/classes/java/main")
        //property("sonar.inclusions", "core/src/main/java/*.java")
        //property("sonar.java.sources", "core/src/main/java/**/.java")
    }
}
