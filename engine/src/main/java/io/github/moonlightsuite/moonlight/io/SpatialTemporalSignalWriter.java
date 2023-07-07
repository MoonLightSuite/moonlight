/**
 *
 */
package io.github.moonlightsuite.moonlight.io;

import java.io.File;
import java.io.IOException;

import io.github.moonlightsuite.moonlight.core.io.DataHandler;
import io.github.moonlightsuite.moonlight.offline.signal.SpatialTemporalSignal;

/**
 * @author loreti
 *
 */
public interface SpatialTemporalSignalWriter {

	<S> void write(DataHandler<S> handler, SpatialTemporalSignal<S> signal, File file)  throws IOException ;

	<S> String stringOf( DataHandler<S> handler, SpatialTemporalSignal<S> signal) ;

}
