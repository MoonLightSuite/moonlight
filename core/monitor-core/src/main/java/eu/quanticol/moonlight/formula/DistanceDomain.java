/**
 *
 */
package eu.quanticol.moonlight.formula;

/**
 * @author loreti
 *
 */
public interface DistanceDomain<R> {

    R zero();

    R infinity();

    boolean lessOrEqual(R x, R y);

    boolean less(R x, R y);

    R sum(R x, R y);

    boolean equalTo(R x, R y);

}
