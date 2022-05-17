package eu.quanticol.moonlight.offline.signal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public abstract class ParallelSignalCursor1<V> {
    protected final List<SignalCursor<Double, V>> cursors;

    protected ParallelSignalCursor1(int size, IntFunction<SignalCursor<Double, V>> f) {
        cursors = new ArrayList<>(size);
        int[] locations = IntStream.range(0, size).toArray();
        initCursors(locations, f);
    }

    protected abstract void initCursors(int[] locations,
                                        IntFunction<SignalCursor<Double, V>> f);

    protected ParallelSignalCursor1(int[] locations,
                                    IntFunction<SignalCursor<Double, V>> f) {
        cursors = new ArrayList<>(locations.length);
        initCursors(locations, f);
    }

    public boolean areSynchronized() {
        return !Double.isNaN(getCurrentTime());
    }

    public Double getCurrentTime() {
        double time = cursors.get(0).getCurrentTime();
        for (SignalCursor<Double, V> c : cursors) {
            if (c.getCurrentTime() != time) {
                return Double.NaN;
            }
        }
        return time;
    }

    /**
     * Moves the cursors to the minimum time above all cursors not yet accessed.
     *
     * @return the minimum time at which all cursors are now synchronized
     */
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

    public void move(Double time) {
        for (SignalCursor<Double, V> c : cursors) {
            c.move(time);
        }
    }

    public void forward() {
        forwardTime();
    }

    public double forwardTime() {
        double time = nextTime();
        if (!Double.isNaN(time)) {
            move(time);
        }
        return time;
    }

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
        return forward ? first > second : first < second;
    }

    public void backward() {
        double time = previousTime();
        if (!Double.isNaN(time)) {
            move(time);
        }
    }

    public Double previousTime() {
        double time = Double.NEGATIVE_INFINITY;
        return subsequentTime(time, false);
    }

    public void revert() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public boolean hasNext() {
        for (SignalCursor<Double, V> c : cursors) {
            if (!c.hasNext()) {
                return false;
            }
        }
        return true;
    }

    public boolean hasPrevious() {
        for (SignalCursor<Double, V> c : cursors) {
            if (!c.hasPrevious()) {
                return false;
            }
        }
        return true;
    }

    public IntFunction<V> getCurrentValue() {
        return l -> getCursorAtLocation(l).getCurrentValue();
    }

    protected SignalCursor<Double, V> getCursorAtLocation(int location) {
        return cursors.get(location);
    }

    public List<SignalCursor<Double, V>> getCursors() {
        return cursors;
    }

    public boolean isCompleted() {
        for (SignalCursor<Double, V> signalCursor : cursors) {
            if (signalCursor.isCompleted()) {
                return true;
            }
        }
        return false;
    }
}
