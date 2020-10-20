package eu.quanticol.moonlight.signal;

public class ImmutableSegment<T> implements SegmentInterface<T> {

    private final double start;
    private final double end;

    private final T value;

    public ImmutableSegment(double start, double end, T value) {
        this.start = start;
        this.end = end;
        this.value = value;
    }


    @Override
    public T getValue() {
        return null;
    }

    @Override
    public T getValueAt(double t) {
        return null;
    }


    public double start() {
        return start;
    }

    public double end() {
        return end;
    }
}
