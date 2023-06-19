plugins {
    id("eu.quanticol.java-library")
}

dependencies {
    implementation(project(":engine"))
    implementation(project(":matlab"))
    implementation("org.n52.matlab:matlab-control:5.0.0")
}
