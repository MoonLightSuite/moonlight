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
 *
 */
public class Signal<T> {

	private LinkedList<Sample<T>> data;
	private Sample<T> last;
	private Double end;
	
	public Signal() {
		this(new LinkedList<>(),Double.NaN);
	}
	
	private Signal( LinkedList<Sample<T>> data, double end) {
		this.data = data;
		this.end = end;
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
			if (!last.value.equals(value)) {
				data.add(newSample);
			}
			last = newSample;
			interval = newSample.time;
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
				time = Math.min(si1.nextTime(), si2.nextTime());
			}
			newSignal.complete(end);
		} 
		return newSignal;
	}

	/**
	 * 
	 * @param f
	 * @param init
	 * @return
	 */
	//TODO: Add comments!
	public <R> Signal<R> iterate( BiFunction<T, R, R> f , R init) {
		Signal<R> newSignal = new Signal<>();
		R current = init;
		for (Sample<T> sample : data) {
			current = f.apply(sample.value, current);
			newSignal.add(sample.time,current);
		}
		newSignal.complete(end);
		return newSignal;
	}

	/**
	 * 
	 * @param f
	 * @param init
	 * @return
	 */
	//TODO: Add comments!
	public <R> Signal<R> iterateBackward( BiFunction<T, R, R> f , R init) {
		LinkedList<Sample<R>> newSignal = new LinkedList<>();
		R current = init;
		Iterator<Sample<T>> iterator = data.descendingIterator();
		while (iterator.hasNext()) {
			Sample<T> next = iterator.next();
			current = f.apply(next.value,current);
			newSignal.addFirst(new Sample<>(next.time,current));
		}
		return new Signal<>(newSignal,this.end);
	}
	
	/**
	 * add to the signal the final time step and value
	 * @param end
	 */
	public void complete(double end) {
		if (!this.end.isNaN()||(this.last==null)||(this.last.time>end)) {
			throw new IllegalArgumentException();
		}
		if (this.last.getTime() != end) {
			this.data.add(new Sample<T>(end,this.last.value));
		}
		this.end = end;
	}
	
	/**
	 * if not end time, put as and time last time
	 */
	public void complete() {
		if (!this.end.isNaN()||(this.last==null)||(this.last.getTime()==this.start())) {
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
			private Sample<T> previous;
			private Sample<T> current;
			private Sample<T> next;

			@Override
			public boolean hasNext() {
				return (current!=null)||(next != null)||iterator.hasNext();
			}

			@Override
			public double nextTime() {
				if (current != null) {
					return current.time;
				}
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
				jump(t);
				return next().getValue();
			}

			@Override
			public void jump(double t) {
				if ((previous != null)&&(previous.getTime()>t)) {
					throw new IllegalArgumentException();
				}
				while (((previous==null)||((next==null)||(t>=next.getTime())))&&(iterator.hasNext())) {
					previous = next;
					next = iterator.next();
				}
				if ((next!=null)&&(next.getTime()==t)) {
					previous = next;
					next = null;
				}
				if (previous != null) {
					current = new Sample<T>(t, previous.getValue());
				}
			}

			@Override
			public Sample<T>  next() {
				shift();
				Sample<T> toReturn = this.current;
				this.previous = this.current;
				this.current = null;
				return toReturn;
			}

			private void shift() {
				if (current == null) {
					if (next == null) {
						current = iterator.next();
					} else {
						current = next;
					}					
					next = (iterator.hasNext()?iterator.next():null);
				}
				
			}
		};
	}

	public int size() {
		return data.size();
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Signal [data=" + data + ", end=" + end + "]";
	}
	
	
	
}
