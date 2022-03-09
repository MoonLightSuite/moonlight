/**
 * 
 */
package eu.quanticol.moonlight.io;

import java.io.File;
import java.io.IOException;

import eu.quanticol.moonlight.offline.signal.RecordHandler;
import eu.quanticol.moonlight.offline.signal.Signal;

/**
 * @author loreti
 *
 */
public interface TemporalSignalReader {
	
	Signal<MoonLightRecord> load(RecordHandler handler, File input) throws IOException, IllegalFileFormatException;
	Signal<MoonLightRecord> load(RecordHandler handler, String input) throws IOException, IllegalFileFormatException;

}
