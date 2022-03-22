/*
 * MoonLight: a light-weight framework for runtime monitoring
 * Copyright (C) 2018-2021
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.quanticol.moonlight.offline.signal;

import eu.quanticol.moonlight.online.signal.TimeChain;
import eu.quanticol.moonlight.core.signal.TimeSignal;
import eu.quanticol.moonlight.online.signal.Update;
import eu.quanticol.moonlight.core.base.Pair;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 *
 */
public class Signal<T> implements TimeSignal<Double, T>
{

    private Segment<T> first;
    private Segment<T> last;
    private int size = 0;
    private double end = 0.0; //TODO: we should just take this from last segment
                              // (unnecessary logic duplication)

    public Signal() {
        this.first = null;
        this.last = null;
        this.size = 0;
        this.end = Double.NaN;
    }

    /**
     * @return the start time of the signal
     */
    public double start() {
        return Segment.getTime(first);
    }

    /**
     * @return the end time of the signal
     */
    public double end() {
        return end;
    }

    /**
     * @return true if the signal is empty
     */
    public boolean isEmpty() {
        return (size == 0);
    }

    /**
     * Add (t,value) to the sample set
     *
     * @param t
     * @param value
     */
    public void add(double t, T value) {
        Segment<T> oldLast = last;
        if (first == null) {
            startWith(t, value);
        } else {
            if (this.end > t) {
                throw new IllegalArgumentException("Time: " + t + " Expected: >" + this.end); //TODO: Add Message!
            }
            last = last.addAfter(t, value);
            end = t;
        }
        if (oldLast != last) {
            size++;
        }
    }

    private void startWith(double t, T value) {
        first = new Segment<>(t, value);
        last = first;
        end = t;
    }


    /**
     * Add (t,value) to the sample set
     *
     * @param t
     * @param value
     */
    public void addBefore(double t, T value) {
        Segment<T> oldFirst = first;
        if (first == null) {
            startWith(t, value);
        } else {
            first = first.addBefore(t, value);
        }
        if (first != oldFirst) {
            size++;
        }
    }

    public <R> Signal<R> iterateForward(BiFunction<T, R, R> f, R init) {
        return map(f, init, true);
    }

    public <R> Signal<R> iterateBackward(BiFunction<T, R, R> f, R init) {
        return map(f, init, false);
    }

    private <R> Signal<R> map(BiFunction<T, R, R> f, R init, boolean forward) {
        Signal<R> newSignal = new Signal<>();
        SignalCursor<Double, T> cursor = getIterator(forward);
        R value = init;
        while (!cursor.isCompleted()) {
            T sValue = cursor.getCurrentValue();
            double t = cursor.getCurrentTime();
            value = f.apply(sValue, value);
            if (forward) {
                newSignal.add(t, value);
                cursor.forward();
            } else {
                newSignal.addBefore(t, value);
                cursor.backward();
            }
        }
        newSignal.end = end;
        return newSignal;
    }

    public void forEach(BiConsumer<Double,T> consumer) {
        SignalCursor<Double, T> cursor = getIterator(true);
        while (!cursor.isCompleted()) {
            consumer.accept(cursor.getCurrentTime(),cursor.getCurrentValue());
            cursor.forward();
        }
    }

    public <R> R reduce(BiFunction<Pair<Double,T>, R, R> reducer, R init) {
        R toReturn = init;
        SignalCursor<Double, T> cursor = getIterator(true);
        while (!cursor.isCompleted()) {
            toReturn = reducer.apply(new Pair<>(cursor.getCurrentTime(), cursor.getCurrentValue()), toReturn);
            cursor.forward();
        }
        return toReturn;
    }

    public <R> void fill( double[] timePoints, R[] data, Function<T,R> f) {
        if (size == 0) {
            throw new IllegalStateException("No array can be generated from an empty signal is empty!");
        }
        Segment<T> current = first;
        for (int i = 0; i < timePoints.length; i++) {
            current = current.jump(timePoints[i]);
            data[i] = f.apply(current.getValue());
        }
    }

    /**
     * @return a new <code>SignalCursor</code>
     *
     * @see SignalCursor
     */
    public SignalCursor<Double, T> getIterator(boolean forward) {
        return new OfflineSignalCursor<>(forward, first, last);
    }

    public int size() {
        return size;
    }


    @Override
    public String toString() {
        if (isEmpty()) {
            return "Signal [ ]";
        } else {
            return "Signal [start=" + start() +
                           ", end=" + end() +
                           ", size=" + size() + "]";
        }
    }

    public void endAt(double end) {
        if ((this.end > end) || (last == null)) {
            throw new IllegalArgumentException(); //TODO: Add message!
        }
        this.end = end;
        this.last.endAt(end);
    }

    @Override
    public T getValueAt(Double time) {
        return (first == null ? null : first.getValueAt(time));
    }

    public double[][] arrayOf(ToDoubleFunction<T> f) {
        if (size == 0) {
            return new double[][] {};
            //throw new IllegalStateException("No array can be generated from an empty signal is empty!");
        }
        int arraySize = size;
        if (!last.isAPoint()) {
            arraySize++;
        }
        double[][] toReturn = new double[arraySize][2];
        Segment<T> current = first;
        int counter = 0;
        while (current != null) {
            toReturn[counter][0] = current.getStart();
            toReturn[counter][1] = f.applyAsDouble( current.getValue() );
            current = current.getNext();
            counter++;
        }
        if (!last.isAPoint()) {
            toReturn[size][0] = end;
            toReturn[size][1] = f.applyAsDouble( last.getValue() );
        }
        return toReturn;
    }

    /**
     * Returns a 2d-array from a set of time points and a conversion-to-double function
     * @param timePoints time points at which the array is sampled
     * @param f function to transform the type of the array T to Double
     * @return a 2d-double-array of [time point][value]
     */
    public double[][] arrayOf(double[] timePoints, ToDoubleFunction<T> f) {
        if (size == 0) {
            return new double[][] {};
            //throw new IllegalStateException("No array can be generated from an empty signal is empty!");
        }
        double[][] toReturn = new double[timePoints.length][2];
        Segment<T> current = first;
        double value = Double.NaN;
        for (int i = 0; i < timePoints.length; i++) {
            if (current != null) {
                current = current.jump(timePoints[i]);
            }
            if (current != null) {
                value = f.applyAsDouble(current.getValue());
            }
            toReturn[i][0] = timePoints[i];
            toReturn[i][1] = value;
        }
        return toReturn;
    }



    public Set<Double> getTimeSet() {
        HashSet<Double> timeSet = new HashSet<>();
        Segment<T> current = first;
        while (current != null) {
            timeSet.add(current.getStart());
            current = current.getNext();
        }
        timeSet.add(end);
        return timeSet;
    }

    public Double getEnd() {
        return end;
    }

    @Override
    public boolean refine(Update<Double, T> u) {
        throw new UnsupportedOperationException("Refinements are not implemented yet for offline signals");
    }

    @Override
    public boolean refine(TimeChain<Double, T> updates) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Refinements are not implemented yet for offline signals");
    }


    @Override
    public TimeChain<Double, T> getSegments() {
        throw new UnsupportedOperationException("Segment extraction is not implemented yet for offline signals");
    }

    @Override
    public TimeChain<Double, T> select(Double from, Double to) {
        throw new UnsupportedOperationException("Selection is not implemented yet for offline signals");
    }
}
