package eu.quanticol.moonlight.domain;

public interface IntervalExtremes<T extends Comparable<T>> {

    AbstractInterval<T> any();
    AbstractInterval<T> empty();
}
