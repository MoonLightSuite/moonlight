package eu.quanticol.moonlight.domain;

import eu.quanticol.moonlight.signal.DataHandler;

public class AbsIntervalDomain<R extends Comparable<R>>
        implements RefinableSignalDomain<AbstractInterval<R>>
{
    private final RefinableSignalDomain<R> domain;

    public AbsIntervalDomain(RefinableSignalDomain<R> domain) {
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
    public AbstractInterval<R> any() {
        return new AbstractInterval<>(domain.min(), domain.max());
    }

    /**
     * Negation function that s.t. De Morgan laws, double negation
     * and inversion of the idempotent elements hold.
     *
     * @param x element to negate
     * @return the negation of the x element
     */
    @Override
    public AbstractInterval<R> negation(AbstractInterval<R> x) {
        return new AbstractInterval<>(domain.negation(x.getEnd()),
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
    public AbstractInterval<R> conjunction(AbstractInterval<R> x, AbstractInterval<R> y) {
        return new AbstractInterval<>(
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
    public AbstractInterval<R> disjunction(AbstractInterval<R> x, AbstractInterval<R> y) {
        return new AbstractInterval<>(
                domain.disjunction(x.getStart(), y.getStart()),
                domain.disjunction(x.getEnd(), y.getEnd()));
    }

    /**
     * @return the infimum (aka meet) of the lattice defined over the semiring.
     */
    @Override
    public AbstractInterval<R> min() {
        return new AbstractInterval<>(domain.min(), domain.min());
    }

    /**
     * @return the supremum (aka join) of the lattice defined over the semiring.
     */
    @Override
    public AbstractInterval<R> max() {
        return new AbstractInterval<>(domain.max(), domain.max());
    }

    /**
     * @return an helper class to manage data parsing over the given type.
     */
    @Override
    public DataHandler<AbstractInterval<R>> getDataHandler() {
        return null;
    }

    @Override
    public boolean equalTo(AbstractInterval<R> x, AbstractInterval<R> y) {
        return false;
    }

    @Override
    public AbstractInterval<R> valueOf(boolean b) {
        return null;
    }

    @Override
    public AbstractInterval<R> valueOf(double v) {
        return null;
    }

    @Override
    public AbstractInterval<R> computeLessThan(double v1, double v2) {
        return null;
    }

    @Override
    public AbstractInterval<R> computeLessOrEqualThan(double v1, double v2) {
        return null;
    }

    @Override
    public AbstractInterval<R> computeEqualTo(double v1, double v2) {
        return null;
    }

    @Override
    public AbstractInterval<R> computeGreaterThan(double v1, double v2) {
        return null;
    }

    @Override
    public AbstractInterval<R> computeGreaterOrEqualThan(double v1, double v2) {
        return null;
    }
}
