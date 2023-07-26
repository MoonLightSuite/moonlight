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


