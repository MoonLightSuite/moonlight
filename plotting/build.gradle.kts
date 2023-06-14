plugins {
    id("eu.quanticol.java-library")
    id("eu.quanticol.generate-docs")
}

dependencies {
    implementation(project(":engine"))
    implementation("com.github.sh0nk:matplotlib4j:0.5.0")
}
repositories {
    mavenCentral()
}

