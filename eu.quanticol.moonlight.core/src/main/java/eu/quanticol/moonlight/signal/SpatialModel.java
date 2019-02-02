/**
 * 
 */
package eu.quanticol.moonlight.signal;

/**
 * @author loreti
 *
 */
public interface SpatialModel<T> {
	
	public T get( int src , int trg );
	
	public int size();

}
