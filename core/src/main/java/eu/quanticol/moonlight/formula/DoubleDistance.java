/**
 * 
 */
package eu.quanticol.moonlight.formula;

/**
 * @author loreti
 *
 */
public class DoubleDistance implements DistanceDomain<Double> {

	@Override
	public Double zero() {
		return 0.0;
	}

	@Override
	public Double infinity() {
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public boolean lessOrEqual(Double x, Double y) {
		return x.doubleValue()<=y.doubleValue();
	}

	@Override
	public Double sum(Double x, Double y) {
		return x+y;
	}

	@Override
	public boolean less(Double x, Double y) {
		return x<y;
	}

}
