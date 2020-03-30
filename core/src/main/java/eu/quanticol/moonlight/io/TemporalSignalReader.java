/**
 * 
 */
package eu.quanticol.moonlight.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import eu.quanticol.moonlight.signal.Record;
import eu.quanticol.moonlight.signal.RecordHandler;
import eu.quanticol.moonlight.signal.Signal;

/**
 * @author loreti
 *
 */
public interface TemporalSignalReader {
	
	Signal<Record> load(RecordHandler handler, File input) throws IOException, IllegalFileFormatException;
	Signal<Record> load(RecordHandler handler, String input) throws IOException, IllegalFileFormatException;

}
