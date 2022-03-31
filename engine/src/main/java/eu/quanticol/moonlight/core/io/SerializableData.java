package eu.quanticol.moonlight.core.io;

import eu.quanticol.moonlight.core.signal.SignalDomain;

import java.util.function.BiFunction;

/**
 * Interface for dealing with the (de)serialization of operators and values
 * from the MoonlightScript
 * @param <R>
 */
public interface SerializableData<R> {
    R valueOf(boolean b);

    R valueOf(double v);

    default R valueOf(int v) {
        return valueOf((double) v);
    }

    /**
     * @return a helper class to manage data parsing over the given type.
     */
    DataHandler<R> getDataHandler();

    R computeLessThan(double v1, double v2);

    R computeLessOrEqualThan(double v1, double v2);

    R computeEqualTo(double v1, double v2);

    R computeGreaterThan(double v1, double v2);

    R computeGreaterOrEqualThan(double v1, double v2);

    static <S> BiFunction<Double, Double, S> getOperator(SignalDomain<S> domain,
                                                         String op) {
        if ("<".equals(op)) {
            return domain::computeLessThan;
        }
        if ("<=".equals(op)) {
            return domain::computeLessOrEqualThan;
        }
        if ("==".equals(op)) {
            return domain::computeEqualTo;
        }
        if (">=".equals(op)) {
            return domain::computeGreaterOrEqualThan;
        }
        if (">".equals(op)) {
            return domain::computeGreaterThan;
        }
        return (x,y) -> domain.min();
    }
}
