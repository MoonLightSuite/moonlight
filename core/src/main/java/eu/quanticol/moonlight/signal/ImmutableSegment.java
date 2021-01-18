package eu.quanticol.moonlight.signal;

import java.util.Objects;

public class ImmutableSegment<T> implements SegmentInterface<T> {

    private final double start;
    //private final double end;

    private final T value;

    //public ImmutableSegment(double start, double end, T value) {
    public ImmutableSegment(double start, T value) {
        this.start = start;
        //this.end = end;
        this.value = value;
    }


    @Override
    public T getValue() {
        return value;
    }


    public double getStart() {
        return start;
    }

    //public double end() {
    //    return end;
    //}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableSegment<?> that = (ImmutableSegment<?>) o;
        return Double.compare(that.start, start) == 0 &&
                value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, value);
    }

    @Override
    public String toString() {
        return "Segment(" +
                "start=" + start +
                ", value=" + value +
                ')';
    }
}
