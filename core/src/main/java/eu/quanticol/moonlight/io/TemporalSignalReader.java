/**
 * 
 */
package eu.quanticol.moonlight.io;

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
	
	public Signal<Record> load( RecordHandler handler, InputStream input ) throws IOException ;

}
