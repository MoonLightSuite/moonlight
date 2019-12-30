/**
 * 
 */
package eu.quanticol.moonlight.io;

import java.io.IOException;
import java.io.InputStream;

import eu.quanticol.moonlight.monitoring.SpatioTemporalMonitorinInput;
import eu.quanticol.moonlight.signal.Record;
import eu.quanticol.moonlight.signal.RecordHandler;
import eu.quanticol.moonlight.signal.Signal;

/**
 * @author loreti
 *
 */
public interface SpatioTemporalSignalReader {
	
	public SpatioTemporalMonitorinInput<Record,Record> load( RecordHandler edgeDataHandler, RecordHandler signalDataHandler, InputStream input ) throws IOException ;

}
