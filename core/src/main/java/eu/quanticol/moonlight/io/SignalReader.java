/**
 * 
 */
package eu.quanticol.moonlight.io;

import java.io.IOException;
import java.io.InputStream;

import eu.quanticol.moonlight.signal.Signal;

/**
 * @author loreti
 *
 */
public interface SignalReader<S> {
	
	public Signal<S> load( InputStream input ) throws IOException ;

}
