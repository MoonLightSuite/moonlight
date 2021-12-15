// == Main project ==
// This is an empty umbrella build including all the component builds.
// This build is not necessarily needed: component builds work independently.

// == Common scripts ==
includeBuild("build-logic")

// == Moonlight Core ==
includeBuild("core")
//includeBuild("utility")  // -> removed: moved into core

// == MoonlightScript ==
includeBuild("moonlightscript")

// == Moonlight APIs ==
includeBuild("api")
////includeBuild("console")  // -> removed: moved into api

// == Examples ==
includeBuild("examples")
