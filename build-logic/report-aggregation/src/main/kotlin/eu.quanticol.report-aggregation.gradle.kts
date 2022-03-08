// Based on the best practice defined at:
// https://docs.gradle.org/current/samples/sample_jvm_multi_project_with_code_coverage.html

plugins {
    id("java")
    id("jacoco")
    id("jacoco-report-aggregation")
}

/*
// Configurations to declare dependencies
val aggregate: Configuration by configurations.creating {
    isVisible = false
    isCanBeResolved = false
    isCanBeConsumed = false
}

// Resolvable configuration to resolve the classes of all dependencies
val classPath: Configuration by configurations.creating {
    isVisible = false
    isCanBeResolved = true
    isCanBeConsumed = false
    extendsFrom(aggregate)
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.CLASSES))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
    }
}

// A resolvable configuration to collect source code
val sourcesPath: Configuration by configurations.creating {
    isVisible = false
    isCanBeResolved = true
    isCanBeConsumed = false
    extendsFrom(aggregate)
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named("source-folders"))
    }
}

// A resolvable configuration to collect JaCoCo coverage data
val coverageDataPath: Configuration by configurations.creating {
    isVisible = false
    isCanBeResolved = true
    isCanBeConsumed = false
    extendsFrom(aggregate)
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named("jacoco-coverage-data"))
    }
}

// Register a code coverage report task to generate the aggregated report
val codeCoverageReport by tasks.registering(JacocoReport::class) {
    additionalClassDirs(classPath.filter { it.isDirectory })
    additionalSourceDirs(sourcesPath.incoming.artifactView { lenient(true) }.files)
    executionData(coverageDataPath.incoming.artifactView { lenient(true) }
                                  .files.filter { it.exists() }
                                  .filter { !it.name.contains("engine2021.jar") })

    reports {
        // xml is usually used to integrate code coverage with
        // other tools like SonarQube, Coveralls or Codecov
        xml.required.set(true)

        // HTML reports can be used to see code coverage
        // without any external tools
        html.required.set(true)
    }
}*/

// Make JaCoCo report generation part of the 'check' lifecycle phase
tasks.check {
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
}
