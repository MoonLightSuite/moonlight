plugins {
    java
    jacoco
    `jacoco-report-aggregation`
    id("org.sonarqube")
}

// Make JaCoCo report generation part of the 'check' lifecycle phase
tasks.check {
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
}

// == Sonarqube settings ==
sonar {
    // TODO: change project key with `project.name` when sonarcloud is properly configured
    properties {
        property("sonar.projectKey", "MoonLightSuite_MoonLight")
        property("sonar.projectName", "Moonlight")
        property("sonar.organization", "moonlightsuite")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sourceEncoding", "UTF-8")

//        property(
//            "sonar.coverage.jacoco.xmlReportPaths",
//            "build/reports/jacoco/testCodeCoverageReport/testCodeCoverageReport.xml"
//        )
    }
}
