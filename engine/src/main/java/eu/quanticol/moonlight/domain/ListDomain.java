package eu.quanticol.moonlight.domain;

import eu.quanticol.moonlight.core.base.AbstractInterval;
import eu.quanticol.moonlight.core.base.Semiring;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.core.base.DataHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ListDomain<T extends Comparable<T>>
        implements SignalDomain<List<AbstractInterval<T>>>
{
    private final
    List<SignalDomain<AbstractInterval<T>>> domains;

    public ListDomain(List<SignalDomain<T>> domains) {
        this.domains = new ArrayList<>();
        for(SignalDomain<T> d: domains) {
            this.domains.add(new AbsIntervalDomain<>(d));
        }
    }

    public ListDomain(int size, SignalDomain<T> domain) {
        this.domains = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            this.domains.add(new AbsIntervalDomain<>(domain));
        }
    }

    /**
     * Negation function s.t. De Morgan laws, double negation
     * and inversion of the idempotent elements hold.
     *
     * @param x element to negate
     * @return the negation of the x element
     */
    @Override
    public List<AbstractInterval<T>> negation(List<AbstractInterval<T>> x) {
        return IntStream.range(0, domains.size())
                        .mapToObj(i -> domains.get(i).negation(x.get(i)))
                        .collect(Collectors.toList());
    }

    /**
     * Associative, commutative, idempotent operator that chooses a value.
     *
     * @param x first available value
     * @param y second available value
     * @return a result satisfying conjunction properties
     */
    @Override
    public List<AbstractInterval<T>> conjunction(List<AbstractInterval<T>> x,
                                                 List<AbstractInterval<T>> y)
    {
        return IntStream.range(0, domains.size())
                        .mapToObj(i -> domains.get(i)
                                              .conjunction(x.get(i), y.get(i)))
                        .collect(Collectors.toList());
    }

    /**
     * Associative, commutative operator that combines values.
     *
     * @param x first value to combine
     * @param y second value to combine
     * @return a result satisfying disjunction properties
     */
    @Override
    public List<AbstractInterval<T>> disjunction(List<AbstractInterval<T>> x,
                                                 List<AbstractInterval<T>> y)
    {
        return IntStream.range(0, domains.size())
                        .mapToObj(i -> domains.get(i)
                                              .disjunction(x.get(i), y.get(i)))
                        .collect(Collectors.toList());
    }

    /**
     * @return the infimum (aka meet) of the lattice defined over the semiring.
     */
    @Override
    public List<AbstractInterval<T>> min() {
        return domains.stream().map(Semiring::min).collect(Collectors.toList());
    }

    /**
     * @return the supremum (aka join) of the lattice defined over the semiring.
     */
    @Override
    public List<AbstractInterval<T>> max() {
        return domains.stream().map(Semiring::max).collect(Collectors.toList());
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
    public List<AbstractInterval<T>> any() {
        return domains.stream().map(SignalDomain::any)
                      .collect(Collectors.toList());
    }

    /**
     * @return an helper class to manage data parsing over the given type.
     */
    @Override
    public DataHandler<List<AbstractInterval<T>>> getDataHandler() {
        return null;
    }

    @Override
    public boolean equalTo(List<AbstractInterval<T>> x, List<AbstractInterval<T>> y) {
        return false;
    }

    @Override
    public List<AbstractInterval<T>> valueOf(boolean b) {
        return null;
    }

    @Override
    public List<AbstractInterval<T>> valueOf(double v) {
        return null;
    }

    @Override
    public List<AbstractInterval<T>> computeLessThan(double v1, double v2) {
        return null;
    }

    @Override
    public List<AbstractInterval<T>> computeLessOrEqualThan(double v1, double v2) {
        return null;
    }

    @Override
    public List<AbstractInterval<T>> computeEqualTo(double v1, double v2) {
        return null;
    }

    @Override
    public List<AbstractInterval<T>> computeGreaterThan(double v1, double v2) {
        return null;
    }

    @Override
    public List<AbstractInterval<T>> computeGreaterOrEqualThan(double v1, double v2) {
        return null;
    }


}
