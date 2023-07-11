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

package io.github.moonlightsuite.moonlight.offline.signal;

import io.github.moonlightsuite.moonlight.offline.algorithms.BooleanOp;
import io.github.moonlightsuite.moonlight.offline.signal.mfr.MfrSignal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

/**
 * Offline spatio-temporal signal
 *
 * @author loreti
 */
public class SpatialTemporalSignal<T> extends MfrSignal<T> {

    /**
     * Builds an `empty` spatio-temporal signal
     *
     * @param size number of locations of the signal
     */
    public SpatialTemporalSignal(int size) {
        this(size, i -> new Signal<>());
    }

    /**
     * Requires <code>f</code> to be defined for all i &lt; size
     *
     * @param size number of locations of the signal
     * @param f    mapping from locations to temporal signals
     */
    public SpatialTemporalSignal(int size, IntFunction<Signal<T>> f) {
        super(size, f);
    }

    public <R> SpatialTemporalSignal<R> apply(BiFunction<T, T, R> f,
                                              SpatialTemporalSignal<T> s) {
        BooleanOp<T, R> op = new BooleanOp<>();
        return generate(s, i -> op.applyBinary(
                getSignalAtLocation(i),
                f,
                s.getSignalAtLocation(i)));
    }

    private <R> SpatialTemporalSignal<R> generate(SpatialTemporalSignal<T> s,
                                                  IntFunction<Signal<R>> f) {
        checkSize(s.getNumberOfLocations());
        return new SpatialTemporalSignal<>(getNumberOfLocations(), f);
    }

    @Override
    public Signal<T> getSignalAtLocation(int location) {
        return getSignals().get(location);
    }

    public <R> SpatialTemporalSignal<R> applyToSignal(
            SpatialTemporalSignal<T> s,
            BiFunction<Signal<T>, Signal<T>, Signal<R>> f) {
        return generate(s, i -> f.apply(
                getSignalAtLocation(i),
                s.getSignalAtLocation(i)));
    }

    public void add(double t, T[] values) {
        checkSize(values.length);
        add(t, (i -> values[i]));
    }

    public void add(double t, IntFunction<T> f) {
        for (int i = 0; i < getNumberOfLocations(); i++) {
            getSignalAtLocation(i).add(t, f.apply(i));
        }
    }

    public void add(double time, List<T> values) {
        checkSize(values.size());
        add(time, values::get);
    }

    @Override
    public <R> SpatialTemporalSignal<R> apply(Function<T, R> f) {
        BooleanOp<T, R> booleanOp = new BooleanOp<>();
        return new SpatialTemporalSignal<>(getNumberOfLocations(),
                i -> booleanOp.applyUnary(getSignalAtLocation(i), f));
    }

    public <R> MfrSignal<R> selectApply(Function<T, R> f, int[] filter) {
        BooleanOp<T, R> booleanOp = new BooleanOp<>();
        IntFunction<Signal<R>> timeSignal =
                i -> booleanOp.applyUnary(getSignals().get(i), f);
        return new MfrSignal<>(getNumberOfLocations(), timeSignal, filter);
    }

    public List<T> valuesAtT(double t) {
        List<T> values = new ArrayList<>(getNumberOfLocations());
        getSignals().forEach(s -> values.add(s.getValueAt(t)));
        return values;
    }

    public <R> SpatialTemporalSignal<R> applyToSignal(Function<Signal<T>,
            Signal<R>> f) {
        return new SpatialTemporalSignal<>(getNumberOfLocations(),
                (i -> f.apply(getSignalAtLocation(i))));
    }

    @Override
    public ParallelSignalCursor<T> getSignalCursor(boolean forward) {
        return new ParallelSignalCursor<>(getNumberOfLocations(),
                (i -> getSignalAtLocation(i).getIterator(forward)));
    }

    public double start() {
        double start = Double.NEGATIVE_INFINITY;
        for (Signal<T> signal : getSignals()) {
            start = Math.max(start, signal.getStart());
        }
        return start;
    }

    public double end() {
        double end = Double.POSITIVE_INFINITY;
        for (Signal<T> signal : getSignals()) {
            end = Math.min(end, signal.getEnd());
        }
        return end;
    }

    /**
     * Returns a 3d-array from a conversion-to-double function
     *
     * @param f function to transform the type of the array T to Double
     * @return a 3d-double-array of [locations][time point][value]
     */
    public double[][][] toArray(ToDoubleFunction<T> f) {
        int locs = getNumberOfLocations();
        double[] timePoints = getTimeArray();
        double[][][] toReturn = new double[locs][][];
        IntStream.range(0, locs)
                .forEach(i ->
                        toReturn[i] = getSignalAtLocation(i).arrayOf(timePoints, f));
        return toReturn;
    }

    public double[] getTimeArray() {
        Set<Double> timeSet = new HashSet<>();
        getSignals().forEach(s -> timeSet.addAll(s.getTimeSet()));
        return timeSet.stream().sorted()
                .distinct()
                .mapToDouble(d -> d).toArray();
    }

    public <R> void fill(double[] timePoints, R[][] data, Function<T, R> f) {
        for (int i = 0; i < getNumberOfLocations(); i++) {
            getSignalAtLocation(i).fill(timePoints, data[i], f);
        }
    }
}
