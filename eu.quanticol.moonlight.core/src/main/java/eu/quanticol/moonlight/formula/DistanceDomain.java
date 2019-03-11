/**
 * 
 */
package eu.quanticol.moonlight.formula;

/**
 * @author loreti
 *
 */
public interface DistanceDomain<R> {

	public R zero();
	
	public R infinity();
	
	public boolean lessOrEqual( R x , R y );

	public boolean less( R x , R y );

	public R sum( R x , R y );
	
}
