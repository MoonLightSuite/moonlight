/**
 *
 */
package eu.quanticol.moonlight.offline.signal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

/**
 * @author loreti
 */
public class ParallelSignalCursor<V> implements
        SignalCursor<Double, IntFunction<V>> {
    private final List<SignalCursor<Double, V>> cursors;

    public ParallelSignalCursor(int size,
                                IntFunction<SignalCursor<Double, V>> f) {
        this.cursors = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.cursors.add(f.apply(i));
        }
    }

    public boolean areSynchronized() {
        return !Double.isNaN(getCurrentTime());
    }

    public double forwardTime() {
        double time = nextTime();
        if (!Double.isNaN(time)) {
            move(time);
        }
        return time;
    }

    public double syncCursors() {
        double time = cursors.get(0).getCurrentTime();
        boolean flag = false;
        for (SignalCursor<Double, V> c : cursors) {
            if (time != c.getCurrentTime()) {
                flag = true;
            }
            time = Math.max(time, c.getCurrentTime());
        }
        if (flag) {
            move(time);
        }
        return time;
    }

    @Override
    public Double nextTime() {
        double time = Double.POSITIVE_INFINITY;
        return subsequentTime(time, true);
    }

    private double subsequentTime(double time, boolean forward) {
        for (SignalCursor<Double, V> c : cursors) {
            double cursorTime = c.nextTime();
            if (Double.isNaN(cursorTime)) {
                return Double.NaN;
            }
            if (isFirstAfterSecond(time, cursorTime, forward)) {
                time = cursorTime;
            }
        }
        return time;
    }

    private boolean isFirstAfterSecond(double first, double second,
                                       boolean forward) {
        if (forward)
            return first > second;
        return first < second;
    }

    @Override
    public Double previousTime() {
        double time = Double.NEGATIVE_INFINITY;
        return subsequentTime(time, false);
    }

    @Override
    public void forward() {
        forwardTime();
    }

    @Override
    public void backward() {
        double time = previousTime();
        if (!Double.isNaN(time)) {
            move(time);
        }
    }

    @Override
    public void revert() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void move(Double time) {
        for (SignalCursor<Double, V> c : cursors) {
            c.move(time);
        }
    }

    @Override
    public boolean hasNext() {
        for (SignalCursor<Double, V> c : cursors) {
            if (!c.hasNext()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasPrevious() {
        for (SignalCursor<Double, V> c : cursors) {
            if (!c.hasPrevious()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Double getCurrentTime() {
        if (cursors.isEmpty()) {
            return Double.NaN;
        }
        double time = cursors.get(0).getCurrentTime();
        for (SignalCursor<Double, V> c : cursors) {
            if (c.getCurrentTime() != time) {
                return Double.NaN;
            }
        }
        return time;
    }

    @Override
    public IntFunction<V> getCurrentValue() {
        return l -> cursors.get(l).getCurrentValue();
    }

    @Override
    public boolean isCompleted() {
        for (SignalCursor<Double, V> signalCursor : cursors) {
            if (signalCursor.isCompleted()) {
                return true;
            }
        }
        return false;
    }
}
