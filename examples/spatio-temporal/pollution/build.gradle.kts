plugins {
    id("eu.quanticol.java-library")
}

dependencies {
    implementation(project(":engine"))
    implementation("com.github.sh0nk:matplotlib4j:0.5.0")
    //compile project(":examples:spatio-temporal:subway")
}
