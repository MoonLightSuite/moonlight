/**
 *
 */
package eu.quanticol.moonlight.formula;

/**
 * @author loreti
 *
 */
public class DoubleDistance implements DistanceDomain<Double> {
    private static final double TOLERANCE = 1E-12;

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
        return x < y || equalTo(x, y);
    }

    @Override
    public Double sum(Double x, Double y) {
        return x + y;
    }

    @Override
    public boolean equalTo(Double x, Double y) {
        return Math.abs(x - y) < TOLERANCE;
    }

    @Override
    public boolean less(Double x, Double y) {
        return x < y;
    }

}
