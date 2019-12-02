/**
 * 
 */
package eu.quanticol.moonlight;

import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;

/**
 * @author loreti
 *
 */
public interface MoonLightScript {
	
	public void monitor( String label, String inputFile , String outputFile );

	public String[] getMonitors();
	
	public String getInfo( String monitor );

}
