package eu.quanticol.moonlight.signal;

public interface SegmentInterface<T> {

    T getValue();

    T getValueAt(double t);

}
