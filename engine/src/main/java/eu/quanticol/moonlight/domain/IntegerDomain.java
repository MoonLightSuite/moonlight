package eu.quanticol.moonlight.domain;

import eu.quanticol.moonlight.core.space.DistanceDomain;

import java.util.Objects;

public class IntegerDomain implements DistanceDomain<Integer> {
    @Override
    public Integer zero() {
        return 0;
    }

    @Override
    public Integer infinity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Integer sum(Integer x, Integer y) {
        return x + y;
    }

    @Override
    public Integer multiply(Integer x, int factor) {
        return x * factor;
    }

    @Override
    public boolean less(Integer x, Integer y) {
        return x < y;
    }

    @Override
    public boolean equalTo(Integer x, Integer y) {
        return Objects.equals(x, y);
    }

    @Override
    public boolean lessOrEqual(Integer x, Integer y) {
        return x <= y;
    }
}

