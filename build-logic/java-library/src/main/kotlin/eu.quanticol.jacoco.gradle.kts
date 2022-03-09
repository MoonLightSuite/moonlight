// Based on the best practice defined at:
// https://docs.gradle.org/current/samples/sample_jvm_multi_project_with_code_coverage.html

plugins {
    java
    jacoco
}

// Do not generate reports for individual projects
tasks.jacocoTestReport.configure {
    enabled = false
}
