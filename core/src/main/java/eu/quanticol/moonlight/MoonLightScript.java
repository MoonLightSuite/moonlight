/**
 * 
 */
package eu.quanticol.moonlight;

/**
 * @author loreti
 *
 */
public interface MoonLightScript {
	
	public void monitor( String label, String inputFile , String outputFile );
	
	public String[] getMonitors();
	
	public String getInfo( String monitor );

}
