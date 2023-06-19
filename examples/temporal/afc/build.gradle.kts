plugins {
    id("eu.quanticol.java-library")
}

val libDir = "../../lib"

dependencies {
    implementation("com.github.sh0nk:matplotlib4j:0.5.0")
    implementation(project(":engine"))
    implementation(project(":matlab"))
    implementation(project(":plotting"))
    implementation(fileTree(mapOf("dir" to libDir, "include" to "*.jar")))
}
