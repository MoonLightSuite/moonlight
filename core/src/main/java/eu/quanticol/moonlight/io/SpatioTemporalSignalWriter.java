/**
 * 
 */
package eu.quanticol.moonlight.io;

import java.io.IOException;
import java.io.OutputStream;

import eu.quanticol.moonlight.signal.DataHandler;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;

/**
 * @author loreti
 *
 */
public interface SpatioTemporalSignalWriter {

	public <S> void write(DataHandler<S> handler, SpatioTemporalSignal<S> signal, OutputStream stream)  throws IOException ;
	
}
