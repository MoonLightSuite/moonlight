/**
 * 
 */
package eu.quanticol.moonlight.io;

import java.io.File;
import java.io.IOException;

import eu.quanticol.moonlight.signal.Record;
import eu.quanticol.moonlight.signal.RecordHandler;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

/**
 * @author loreti
 *
 */
public interface SpatialTemporalSignalReader {

	SpatialTemporalSignal<Record> load(int size, RecordHandler handler, File input) throws IOException, IllegalFileFormatException;
	SpatialTemporalSignal<Record> load(int size, RecordHandler handler, String input) throws IllegalFileFormatException;

}
