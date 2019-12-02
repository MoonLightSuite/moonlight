/**
 * 
 */
package eu.quanticol.moonlight.io;

import java.io.IOException;
import java.io.OutputStream;

import eu.quanticol.moonlight.signal.Signal;

/**
 * @author loreti
 *
 */
public interface SignalWriter<S> {

	public void write(Signal<S> signal, OutputStream stream)  throws IOException ;
	
}
