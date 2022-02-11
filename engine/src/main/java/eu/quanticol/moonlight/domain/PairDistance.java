package eu.quanticol.moonlight.domain;

import eu.quanticol.moonlight.util.Pair;

public class PairDistance<D1,D2> implements DistanceDomain<Pair<D1,D2>> {

    private final DistanceDomain<D1> firstDomain;
    private final DistanceDomain<D2> secondDomain;

    public PairDistance(DistanceDomain<D1> firstDomain, DistanceDomain<D2> secondDomain) {
        this.firstDomain = firstDomain;
        this.secondDomain = secondDomain;
    }

    @Override
    public Pair<D1, D2> zero() {
        return new Pair<>(firstDomain.zero(),secondDomain.zero());
    }

    @Override
    public Pair<D1, D2> infinity() {
        return new Pair<>(firstDomain.infinity(),secondDomain.infinity());
    }

    @Override
    public boolean lessOrEqual(Pair<D1, D2> x, Pair<D1, D2> y) {
        return firstDomain.lessOrEqual(x.getFirst(),y.getFirst())&&secondDomain.lessOrEqual(x.getSecond(), y.getSecond());
    }

    @Override
    public boolean less(Pair<D1, D2> x, Pair<D1, D2> y) {
        return firstDomain.less(x.getFirst(),y.getFirst())&&secondDomain.less(x.getSecond(), y.getSecond());
    }

    @Override
    public Pair<D1, D2> sum(Pair<D1, D2> x, Pair<D1, D2> y) {
        return new Pair<>(firstDomain.sum(x.getFirst(),y.getFirst()),secondDomain.sum(x.getSecond(),y.getSecond()));
    }

    @Override
    public boolean equalTo(Pair<D1, D2> x, Pair<D1, D2> y) {
        return firstDomain.equalTo(x.getFirst(),y.getFirst())&&secondDomain.equalTo(x.getSecond(), y.getSecond());
    }

}
