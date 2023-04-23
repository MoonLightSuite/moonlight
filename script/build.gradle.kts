plugins {
    id("eu.quanticol.java-library")
    antlr
    kotlin("jvm") version "1.8.20"          // for compiling the docs
}

dependencies {
    implementation(project(":engine"))
    implementation("org.antlr:ST4:4.3")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

    // use ANTLR version 4
    antlr("org.antlr:antlr4:4.8")
    implementation("org.antlr:antlr4-runtime:4.8")

}

tasks.build {
    dependsOn(tasks.generateGrammarSource)
}

tasks.sourcesJar {
    dependsOn(tasks.generateGrammarSource)
}

tasks.generateGrammarSource {
    arguments.addAll(listOf("-visitor", "-long-messages"))
}
