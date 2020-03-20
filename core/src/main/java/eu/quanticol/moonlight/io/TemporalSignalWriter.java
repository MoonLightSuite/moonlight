/**
 * 
 */
package eu.quanticol.moonlight.io;

import java.io.IOException;
import java.io.OutputStream;

import eu.quanticol.moonlight.signal.DataHandler;
import eu.quanticol.moonlight.signal.Signal;

/**
 * @author loreti
 *
 */
public interface TemporalSignalWriter {

	<S> void write(DataHandler<S> handler, Signal<S> signal, OutputStream stream)  throws IOException ;
	
}
