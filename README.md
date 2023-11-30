# MoonLight ![Build Status](https://github.com/MoonLightSuite/MoonLight/actions/workflows/build.yml/badge.svg) [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=MoonLightSuite_MoonLight&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=MoonLightSuite_MoonLight) [![codecov](https://codecov.io/gh/MoonLightSuite/MoonLight/branch/master/graph/badge.svg)](https://codecov.io/gh/MoonLightSuite/MoonLight)

MoonLight is a light-weight Java-tool for monitoring temporal, spatial and spatio-temporal properties of distributed complex systems, as *Cyber-Physical Systems* and *Collective Adaptive Systems*.

It supports the specification of properties written with the *Reach and Escape Logic* ([STREL](https://dl.acm.org/citation.cfm?id=3127050)). STREL is a linear time temporal logic, in particular, it extends the *Signal Temporal Logic*
 ([STL](https://link.springer.com/chapter/10.1007/978-3-642-15297-9_9)) with a number of spatial operators that permit to described complex spatial behaviors as beeing surround, reaching  target locations and escaping from specific regions. 
<!-- The monitoring procedure is done with respect a single spatio-temporal trajecotry. Given a spatial configuration, a trajectory and a property the tool returns a spatio-temporal signal that describes the satisfaction of the property in each location and at each time.
The tool supports two type of semantics (satisfaction), the Boolean and the quantitative semantics.
Choosing the Boolean semantics the tool returns a Boolean satisfaction signal, that tells at each time in each location if the trajectory satisfies or not the property, choosing instead the Quantitative semantics the tool returns a real-value signal that corresponds to the value of satisfaction of the property.
-->

### Prerequisites
It requires Java 21+ in the running environment

### Usage as Maven package

To use it as a maven package, just include the following line in your `build.gradle.kts`:

```kts
implementation("io.github.moonlightsuite:moonlight-engine:v0.2.0")
```

Check on [Maven Central](https://central.sonatype.com/artifact/io.github.moonlightsuite/moonlight-engine/) for alternative build tools (
e.g. Maven).

For more information, please visit our [Wiki](https://github.com/MoonLightSuite/MoonLight/wiki) 


### Usage as Python Package
You can install MoonLight as a Python package using pip:

```shell
pip install moonlight
```