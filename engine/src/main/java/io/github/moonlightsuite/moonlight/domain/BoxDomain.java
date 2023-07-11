package io.github.moonlightsuite.moonlight.domain;

import io.github.moonlightsuite.moonlight.core.base.Box;
import io.github.moonlightsuite.moonlight.core.signal.SignalDomain;
import io.github.moonlightsuite.moonlight.core.io.DataHandler;

public class BoxDomain<R extends Comparable<R>>
        implements SignalDomain<Box<R>>
{
    private final SignalDomain<R> domain;

    public BoxDomain(SignalDomain<R> domain) {
        this.domain = domain;
    }

    /**
     * Unknown element: this is an element of the set that represents
     * undefined areas of the signal.
     * Examples of this could be 0 for real numbers,
     * a third value for booleans, or the total interval for intervals.
     *
     * @return the element of the set representing absence of knowledge
     */
    @Override
    public Box<R> any() {
        return new Box<>(domain.min(), domain.max());
    }

    /**
     * Negation function that s.t. De Morgan laws, double negation
     * and inversion of the idempotent elements hold.
     *
     * @param x element to negate
     * @return the negation of the x element
     */
    @Override
    public Box<R> negation(Box<R> x) {
        return new Box<>(domain.negation(x.getEnd()),
                                      domain.negation(x.getStart()));
    }



    /**
     * Associative, commutative, idempotent operator that chooses a value.
     *
     * @param x first available value
     * @param y second available value
     * @return a result satisfying conjunction properties
     */
    @Override
    public Box<R> conjunction(Box<R> x, Box<R> y) {
        return new Box<>(
                domain.conjunction(x.getStart(), y.getStart()),
                domain.conjunction(x.getEnd(), y.getEnd()));
    }

    /**
     * Associative, commutative operator that combines values.
     *
     * @param x first value to combine
     * @param y second value to combine
     * @return a result satisfying disjunction properties
     */
    @Override
    public Box<R> disjunction(Box<R> x, Box<R> y) {
        return new Box<>(
                domain.disjunction(x.getStart(), y.getStart()),
                domain.disjunction(x.getEnd(), y.getEnd()));
    }

    /**
     * @return the infimum (aka meet) of the lattice defined over the semiring.
     */
    @Override
    public Box<R> min() {
        return new Box<>(domain.min(), domain.min());
    }

    /**
     * @return the supremum (aka join) of the lattice defined over the semiring.
     */
    @Override
    public Box<R> max() {
        return new Box<>(domain.max(), domain.max());
    }

    /**
     * @return an helper class to manage data parsing over the given type.
     */
    @Override
    public DataHandler<Box<R>> getDataHandler() {
        return notImplemented();
    }

    @Override
    public boolean equalTo(Box<R> x, Box<R> y) {
        return x.equals(y);
    }

    @Override
    public Box<R> valueOf(boolean b) {
        return notImplemented();
    }

    @Override
    public Box<R> valueOf(double v) {
        return notImplemented();
    }

    @Override
    public Box<R> computeLessThan(double v1, double v2) {
        return notImplemented();
    }

    @Override
    public Box<R> computeLessOrEqualThan(double v1, double v2) {
        return notImplemented();
    }

    @Override
    public Box<R> computeEqualTo(double v1, double v2) {
        return notImplemented();
    }

    @Override
    public Box<R> computeGreaterThan(double v1, double v2) {
        return notImplemented();
    }

    @Override
    public Box<R> computeGreaterOrEqualThan(double v1, double v2) {
        return notImplemented();
    }

    private <T> T notImplemented() {
        throw new UnsupportedOperationException("Operation not implemented.");
    }
}
