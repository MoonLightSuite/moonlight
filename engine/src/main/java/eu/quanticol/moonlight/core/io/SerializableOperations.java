package eu.quanticol.moonlight.core.io;

import eu.quanticol.moonlight.core.signal.SignalDomain;

import java.util.function.BiFunction;

public interface SerializableOperations<R> {
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
