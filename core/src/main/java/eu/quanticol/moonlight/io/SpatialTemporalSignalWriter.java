/**
 * 
 */
package eu.quanticol.moonlight.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import eu.quanticol.moonlight.signal.DataHandler;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

/**
 * @author loreti
 *
 */
public interface SpatialTemporalSignalWriter {

	<S> void write(DataHandler<S> handler, SpatialTemporalSignal<S> signal, File file)  throws IOException ;

	<S> String stringOf( DataHandler<S> handler, SpatialTemporalSignal<S> signal) ;
	
}
