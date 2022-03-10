plugins {
    java
    jacoco
    `jacoco-report-aggregation`
}

// Make JaCoCo report generation part of the 'check' lifecycle phase
tasks.check {
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
}
