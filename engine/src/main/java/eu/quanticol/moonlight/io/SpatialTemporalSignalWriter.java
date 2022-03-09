/**
 * 
 */
package eu.quanticol.moonlight.io;

import java.io.File;
import java.io.IOException;

import eu.quanticol.moonlight.offline.signal.DataHandler;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;

/**
 * @author loreti
 *
 */
public interface SpatialTemporalSignalWriter {

	<S> void write(DataHandler<S> handler, SpatialTemporalSignal<S> signal, File file)  throws IOException ;

	<S> String stringOf( DataHandler<S> handler, SpatialTemporalSignal<S> signal) ;
	
}
