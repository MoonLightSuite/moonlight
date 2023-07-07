plugins {
    id("io.github.moonlightsuite.java-library")
}

dependencies {
    implementation(project(":engine"))
    implementation(project(":matlab"))
    implementation(project(":script"))
    implementation("org.n52.matlab:matlab-control:5.0.0")
}
