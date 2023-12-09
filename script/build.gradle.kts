plugins {
    antlr
    id("io.github.moonlightsuite.java-library")
//    id("io.github.moonlightsuite.generate-docs")
    id("io.github.moonlightsuite.publish")
    id("org.beryx.jlink") version "3.0.1"
}

jlink {
    launcher {
        name = "moonlight"
        jvmArgs = listOf("-Dlogback.configurationFile=./logback.xml")
    }
}

dependencies {
    implementation(project(":engine"))
//    implementation("org.antlr:ST4:4.3")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

    // use ANTLR version 4
    antlr("org.antlr:antlr4:4.8")
    runtimeOnly("org.antlr:antlr4-runtime:4.8")
}

tasks.generateGrammarSource {
    arguments.addAll(listOf("-visitor", "-long-messages"))
}

tasks.build {
    dependsOn(tasks.generateGrammarSource)
}
//
//tasks.kotlinSourcesJar {
//    dependsOn(tasks.generateGrammarSource)
//}
//
//tasks.dokkaHtml {
//    dependsOn(tasks.generateGrammarSource)
//}
