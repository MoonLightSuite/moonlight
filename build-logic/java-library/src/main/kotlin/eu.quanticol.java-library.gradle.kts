plugins {
    id("java")
   // id("java-platform")
    id("eu.quanticol.jacoco")
}

group = "eu.quanticol.moonlight"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
//    implementation(platform("java-platform"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
