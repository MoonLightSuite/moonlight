package eu.quanticol.moonlight.signal.online;

import java.util.Objects;

public class ImmutableSegment<V> implements SegmentInterface<Double, V> {
    private final double start;
    //private final double end;

    private final V value;

    //public ImmutableSegment(double start, double end, T value) {
    public ImmutableSegment(Double start, V value) {
        this.start = start;
        //this.end = end;
        this.value = value;
    }


    @Override
    public V getValue() {
        return value;
    }


    public Double getStart() {
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
