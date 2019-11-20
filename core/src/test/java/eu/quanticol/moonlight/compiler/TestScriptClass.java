/**
 * 
 */
package eu.quanticol.moonlight.compiler;

import eu.quanticol.moonlight.MoonLightScript;
import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;

/**
 * @author loreti
 *
 */
public class TestScriptClass implements MoonLightScript {

	@Override
	public void monitor(String label, String inputFile, String outputFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void monitor(String label, Signal<?> signal, String outputFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void monitor(String label, LocationService<?> service, SpatioTemporalSignal<?> signal, String outputFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] getMonitors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInfo(String monitor) {
		// TODO Auto-generated method stub
		return null;
	}

}
