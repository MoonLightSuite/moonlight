/**
 * 
 */
package eu.quanticol.moonlight.signal;

import java.util.List;
import java.util.Set;

import eu.quanticol.moonlight.util.Pair;

/**
 * @author loreti
 *
 */
public interface SpatialModel<T> {
	
	public T get( int src , int trg );
	
	public int size();
	
	public List<Pair<Integer,T>> next( int l );

	public List<Pair<Integer,T>> previous( int l );

	public Set<Integer> getLocations();

}
