/**
 * 
 */
package eu.quanticol.moonlight.signal;

import java.util.Iterator;

import eu.quanticol.moonlight.util.Pair;

/**
 * @author loreti
 *
 */
public interface LocationService<V> {
	
	public SpatialModel<V> get(double t);
	
	public Iterator<Pair<Double, SpatialModel<V>>> times();

	public boolean isEmpty();

}
