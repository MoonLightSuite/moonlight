/*******************************************************************************
 * MoonLight: a light-weight framework for runtime monitoring
 * Copyright (C) 2018 
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
 *******************************************************************************/
package eu.quanticol.moonlight.signal;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author loreti
 *
 */
public class Signal<T> {

	private LinkedList<Sample<T>> data;
	private Sample<T> last;
	private Double end;
	
	public Signal() {
		this.data = new LinkedList<>();
		this.end = Double.NaN;
	}
	
	
	/**
	 * 
	 * @return the start time of the signal
	 */
	public double start() {
		if (data.isEmpty()) {
			return Double.NaN;
		}
		return data.getFirst().time;
	}
	 
	/**
	 * 
	 * @return the end time of the signal
	 */
	public double end() {
		if (!end.isNaN()) {
			return end;
		}
		if (last == null) {
			return Double.NaN;
		}
		return last.time;
	}
	
	/**
	 * 
	 * @returns true if the signal is empty
	 */
	public boolean isEmpty() {
		return data.isEmpty();
	}
	
	/**
	 * Add (t,value) to the sample set
	 * @param t
	 * @param value
	 * @return last time step
	 */
	public double add( double t , T value ) {
		if (!end.isNaN()) {
			throw new IllegalArgumentException();
		}
		Sample<T> newSample = new Sample<T>(t, value);
		double interval = 0.0;
		if (last == null) {
			last = newSample;			
			data.add(newSample);
			interval = t;
		} else {
			if (last.time>t) {
				throw new IllegalArgumentException();
			} 
			if (last.value.equals(value)) {
				interval = last.time;
			} else {
				data.add(newSample);
				last = newSample;
				interval = newSample.time;
			}
		}
		return interval;
	}
	
	/**
	 * return a signal, given a signal and a function 
	 */
	public <R> Signal<R> apply( Function<T, R> f ) {
		Signal<R> newSignal = new Signal<>();
		for (Sample<T> sample : data) {
			newSignal.add(sample.time, f.apply(sample.value));
		}
		if (!end.isNaN()) {
			newSignal.complete( end );
		}
		return newSignal;
	}
	
	/**
	 * 
	 */
	public static <T,R> Signal<R> apply( Signal<T> s , Function<T,R> f ) {
		return s.apply(f);
	}
	
	/**
	 * return a signal, given two signals and a bifunction 
	 */
	public static <T,R> Signal<R> apply( Signal<T> s1 , BiFunction<T, T, R> f , Signal<T> s2 ) {
		Signal<R> newSignal = new Signal<>();
		SignalIterator<T> si1 = s1.getIterator();
		SignalIterator<T> si2 = s2.getIterator();
		
		double time = Math.max(s1.start(), s2.start());
		double end = Math.min(s1.end(), s2.end());
		if (time<end) {
			si1.jump(time);
			si2.jump(time);
			while ((time < end)) {
				T v1 = si1.next(time);
				T v2 = si2.next(time);
				newSignal.add(time, f.apply(v1, v2));
				time = Math.min(si1.next(), si2.next());
			}
			newSignal.complete(end);
		} 
		return newSignal;
	}
	
	/**
	 * add to the signal the final time step and value
	 * @param end
	 */
	public void complete(double end) {
		if (!this.end.isNaN()||(this.last==null)||(this.last.time>=end)) {
			throw new IllegalArgumentException();
		}
		this.data.add(new Sample<T>(end,this.last.value));
		this.end = end;
	}
	
	/**
	 * if not end time, put as and time last time
	 */
	public void complete() {
		if (!this.end.isNaN()||(this.data.size()<2)) {
			throw new IllegalArgumentException();
		}
		this.end = this.last.time;
	}

	/**
	 * 
	 * @return
	 */
	public SignalIterator<T> getIterator() {
		return new SignalIterator<T>() {
			
			private Iterator<Sample<T>> iterator = data.iterator();
			private Sample<T> current;
			private Sample<T> next;

			@Override
			public boolean hasNext() {
				return (next != null)||iterator.hasNext();
			}

			@Override
			public double next() {
				if (next == null) {
					if (iterator.hasNext()) {
						next = iterator.next();
					} else {
						return Double.NaN;
					}
				} 
				return next.time;
			}

			@Override
			public T next(double t) {
				if (t==next()) {
					current = next;
					next = null;
				} else {
					if ((current != null)&&(t>=current.time)&&(t<next.time)) {
						current = new Sample<T>(t,current.value);
					} else {
						throw new IllegalArgumentException();
					}
				}
				return current.value;
			}

			@Override
			public void jump(double t) {
				if (t<start()) {
					throw new IllegalArgumentException();
				}
				if (t<end()) {
					double nextTime = next();
					while (t > nextTime) {
						next(nextTime);
						nextTime = next();
					} 
				} else {
					throw new IllegalArgumentException();
				}
			}
		};
	}

	public int size() {
		return data.size();
	}
	
}
