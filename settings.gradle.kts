// == Main project ==
// This is an empty umbrella build including all the component builds.
// This build is not necessarily needed.
// The component builds work independently.

includeBuild("build-logic")

includeBuild("core")
//includeBuild("utility")  // -> removed: moved into core

//includeBuild("api")
////includeBuild("console")  // -> removed: moved into api
//
//includeBuild("moonlightscript")
//
//includeBuild("examples")

// TODO: add these
//include ("examples:temporal")
//include ("examples:temporal:amt")
//include ("examples:temporal:matlab")
//include ("examples:spatio-temporal")
//include ("examples:spatio-temporal:bikes")
//include ("examples:spatio-temporal:city")
//include ("examples:spatio-temporal:patterns")
//include ("examples:spatio-temporal:sensors")
//include ("examples:spatio-temporal:subway")
//include ("examples:spatio-temporal:epidemic")
//include ("distribution_files:java")
//include ("examples:temporal:simpleTemporal")
//findProject(":examples:temporal:simpleTemporal")?.name = "simpleTemporal"
//include ("examples:spatio-temporal:simpleGrid")
//findProject(":examples:spatio-temporal:simpleGrid")?.name = "simpleGrid"