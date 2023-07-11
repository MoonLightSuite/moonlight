plugins {
    id("io.github.moonlightsuite.java-library")
    id("io.github.moonlightsuite.generate-docs")
}

dependencies {
    implementation(project(":engine"))
    implementation("com.github.sh0nk:matplotlib4j:0.5.0")
}
