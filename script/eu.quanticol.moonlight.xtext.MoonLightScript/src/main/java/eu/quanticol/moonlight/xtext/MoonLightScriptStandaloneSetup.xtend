/*
 * generated by Xtext 2.18.0.M3
 */
package eu.quanticol.moonlight.xtext


/**
 * Initialization support for running Xtext languages without Equinox extension registry.
 */
class MoonLightScriptStandaloneSetup extends MoonLightScriptStandaloneSetupGenerated {

	def static void doSetup() {
		new MoonLightScriptStandaloneSetup().createInjectorAndDoEMFRegistration()
	}
}