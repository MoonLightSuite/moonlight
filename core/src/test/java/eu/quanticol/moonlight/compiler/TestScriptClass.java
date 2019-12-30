/**
 * 
 */
package eu.quanticol.moonlight.compiler;


import eu.quanticol.moonlight.MoonLightScript;
import eu.quanticol.moonlight.SpatioTemporalScriptComponent;
import eu.quanticol.moonlight.TemporalScriptComponent;

/**
 * @author loreti
 *
 */
public class TestScriptClass extends MoonLightScript {

	public TestScriptClass() {
		super(new String[0], new String[0]);
	}

	@Override
	protected TemporalScriptComponent<?> selectTemporalComponent(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SpatioTemporalScriptComponent<?> selectSpatioTemporalComponent(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected TemporalScriptComponent<?> selectDefaultTemporalComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SpatioTemporalScriptComponent<?> selectDefaultSpatioTemporalComponent() {
		// TODO Auto-generated method stub
		return null;
	}


}
