// Based on the best practice defined at:
// https://docs.gradle.org/current/samples/sample_jvm_multi_project_with_code_coverage.html

plugins {
    java
    jacoco
    `jacoco-report-aggregation`
}

// Make JaCoCo report generation part of the 'check' lifecycle phase
tasks.check {
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
}
