plugins {
    id("eu.quanticol.java-library")
}

val libDir = "../../lib"
val deps = listOf("jsstl.core.jar", "SimHyA.jar")

dependencies {
    implementation("eu.quanticol.moonlight.core:monitor-core")
    implementation("eu.quanticol.moonlight.script:parser")

    implementation(fileTree(mapOf("dir" to libDir, "include" to deps)))

    /* JSSTL Dependencies */
    implementation("org.jgrapht:jgrapht-core:0.9.2")
    implementation("org.jgrapht:jgrapht-ext:0.9.2")

    /* SimHyA Dependencies */
    implementation("org.sbml.jsbml:jsbml:1.5")
    implementation("org.jfree:jfreechart:1.5.0")
    implementation("colt:colt:1.2.0")
    implementation("org.apache.commons:commons-math:2.2")
}
