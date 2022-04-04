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

import eu.quanticol.moonlight.offline.algorithms.BooleanOp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.*;
import java.util.stream.IntStream;

/**
 * Offline spatio-temporal signal
 * @author loreti
 */
public class SpatialTemporalSignal<T> {
	
	private final List<Signal<T>> signals;
	private final int size;

	public SpatialTemporalSignal(int size) {
		this(size, i -> new Signal<>());
	}
	
	public SpatialTemporalSignal(int size, IntFunction<Signal<T>> f) {
		this.signals = new ArrayList<>(size);
		this.size = size;
		init(f);
	}

	private void init(IntFunction<Signal<T>> initFunction) {
		for(int i = 0; i < size; i++) {
			signals.add(i, initFunction.apply(i));
		}
	}
	
	public SpatialTemporalSignal(int size, double[] t, DoubleFunction<T[]> f) {
		this(size);
		for (double v : t) {
			add(v, f.apply(v));
		}
	}

	public SpatialTemporalSignal(int size, double[] t, T[][] m) {
		this.signals = new ArrayList<>(size);
		this.size = size;
		for(int i = 0; i < t.length; i++) {
			add(t[i], m[i]);
		}
	}

	public int getNumberOfLocations() {
		return size;
	}
	
	public void add(double t, T[] values) {
		checkSize(values.length, size);
		add(t, (i -> values[i]));
	}

	public void add(double time, List<T> values) {
		checkSize(values.size(), size);
		add(time, values::get);
	}

	private static void checkSize(int inputSize, int baseSize) {
		if (inputSize != baseSize) {
			throw new IllegalArgumentException("Input mismatch with signal " +
											   "size");
		}

	}

	public void add(double t, IntFunction<T> f) {
		for(int i = 0; i < size; i++) {
			signals.get(i).add(t, f.apply(i));
		}
	}


	public List<Signal<T>> getSignals(){return signals;}

	public List<T> valuesatT(double t){
		List<T> spSignal = new ArrayList<>(size);
		for(int i = 0; i < size; i++) {
			spSignal.add(signals.get(i).getValueAt(t));
		}
		return spSignal;
	}

	public <R> SpatialTemporalSignal<R> apply(Function<T, R> f) {
		BooleanOp<T, R> booleanOp = new BooleanOp<>();
		return new SpatialTemporalSignal<>(this.size,
				(i -> booleanOp.applyUnary(signals.get(i), f)));
	}
	
	public static <T, R> SpatialTemporalSignal<R> apply(
			SpatialTemporalSignal<T> s1,
			BiFunction<T, T, R> f,
			SpatialTemporalSignal<T> s2)
	{
		checkSize(s1.size(), s2.size());
		BooleanOp<T, R> booleanOp = new BooleanOp<>();
		return new SpatialTemporalSignal<>(s1.size,
				(i -> booleanOp.applyBinary(s1.signals.get(i), f,
						s2.signals.get(i))));
	}

	public static <T, R> SpatialTemporalSignal<R> applyToSignal(
			SpatialTemporalSignal<T> s1,
			BiFunction<Signal<T>, Signal<T>, Signal<R>> f,
			SpatialTemporalSignal<T> s2)
	{
		checkSize(s1.size(), s2.size());
		return new SpatialTemporalSignal<>(s1.size,
				(i -> f.apply( s1.signals.get(i), s2.signals.get(i))));
	}

	public <R> SpatialTemporalSignal<R> applyToSignal(Function<Signal<T>,
			Signal<R>> f)
	{
		return new SpatialTemporalSignal<>(this.size,
				(i -> f.apply(signals.get(i))));
	}
	
	public ParallelSignalCursor<T> getSignalCursor(boolean forward) {
		return new ParallelSignalCursor<>(signals.size(),
				(i -> signals.get(i).getIterator(forward)));
	}

	public double start() {
		double start = Double.NEGATIVE_INFINITY;
		for (Signal<T> signal: signals) {
			start = Math.max(start, signal.getStart());
		}
		return start;
	}

	public double end() {
		double end = Double.POSITIVE_INFINITY;
		for (Signal<T> signal: signals) {
			end = Math.min(end, signal.getEnd());
		}
		return end;
	}

	public void endAt(double end) {
		for (Signal<T> signal: signals) {
			signal.endAt(end);
		}
	}


	/**
	 * Returns a 3d-array from a conversion-to-double function
	 * @param f function to transform the type of the array T to Double
	 * @return a 3d-double-array of [locations][time point][value]
	 */
	public double[][][] toArray(ToDoubleFunction<T> f) {
		double[] timePoints = getTimeArray();
		double[][][] toReturn = new double[size][][];
		IntStream.range(0, size)
				 .forEach(i ->
						  toReturn[i] = signals.get(i).arrayOf(timePoints,f));
		return toReturn;
	}

	public double[] getTimeArray() {
		Set<Double> timeSet = new HashSet<>();
		for (Signal<T> s: this.signals) {
			timeSet.addAll(s.getTimeSet());
		}
		return timeSet.stream().sorted()
					  .distinct()
					  .mapToDouble(d -> d).toArray();
	}

	public <R> void fill( double[] timePoints, R[][] data, Function<T, R> f) {
		for(int i = 0; i < size; i++) {
			signals.get(i).fill(timePoints, data[i], f);
		}
	}

	public int size() {
		return size;
	}
}
