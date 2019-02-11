/**
 * 
 */
package eu.quanticol.moonlight.formula;

/**
 * @author loreti
 *
 */
public class TropicalSemiring implements Semiring<Double> {

	@Override
	public Double conjunction(Double x, Double y) {
		return x+y;
	}

	@Override
	public Double disjunction(Double x, Double y) {
		return Math.min(x, y);
	}

	@Override
	public Double min() {
		return 0.0;
	}

	@Override
	public Double max() {
		return Double.POSITIVE_INFINITY;
	}

}
