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
package eu.quanticol.moonlight.algorithms;

import java.util.LinkedList;
import java.util.function.BiFunction;

import eu.quanticol.moonlight.signal.Sample;
import eu.quanticol.moonlight.signal.Segment;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SignalCursor;

/**
 *
 */
public class OldSlidingWindow<R> {

	private final double a;
	private final double size;

	/**
	 * A valid aggregator must be a function 'f' that is:
	 * - commutative: f(a,b)=f(b,a)
	 * - idempotent in one of the two arguments: for any a,b, f(a,b)=a || f(a,b)=b
	 */
	private BiFunction<R, R, R> aggregator;
	private boolean isFuture;

	public OldSlidingWindow(double a, double b, BiFunction<R, R, R> aggregator, boolean isFuture) {
		this.a = a;
		this.size = b-a;
		this.aggregator = aggregator;
		this.isFuture = isFuture;
	}

	public Signal<R> apply(Signal<R> s) {
		Signal<R> result = new Signal<>();
		if (s.isEmpty()||(s.end()-s.start()<size)) {
			return result;
		}
		SignalCursor<R> iterator = s.getIterator(true);
		InnerWindow window = new InnerWindow();
		iterator.move(initTime(s.start()));
		while (!iterator.completed()) {
			double time = iterator.time();
			R value = iterator.value();
			while (!window.add(time, value)) {
				result.add(timeOf(window.firstTime()), window.firstValue());
				window.shift( time );
			}
			iterator.forward();
		}
		if (isFuture) {
			result.add(timeOf(window.firstTime()), window.firstValue());
		} else {
			result.add(window.end, window.firstValue());
		}
		return result;
	}

	private double initTime(double start) {
		return start+a;
	}

	private double timeOf(double t) {
		if (isFuture) {
			return t-a;
		} else {
			return t+size;
//			return t+a+size;
		}
	}

	public double size() {
		return size;
	}

	public class InnerWindow {

		private static final double EPSILON = 0.000001;

		private Segment<R> first;

		private Segment<R> last;

		private double end;

		public InnerWindow() {
		}

		public void shift(double time) {
			double nextTime = first.getSegmentEnd();
			if (first.getTime()==nextTime) { // Double.isNaN(nextTime)) {//Window contains a single element!
				init( time-size , first.getValue() );
			} else {
				if (nextTime+size>time) {
					first = first.splitAt(time-size);
				} else {
					first = first.getNext();
					if (first != null) {
						first.setFirst();
					}
				}
			}
		}

		public double firstTime() {
			return first.getTime();
		}

		public R firstValue() {
			return first.getValue();
		}

		public double size( ) {
			return (first == null?0.0:end-first.getTime());
		}

		public boolean add( double time , R value ) {
			if (first==null) {
				init( time, value );
			} else {
//				if (Math.abs(first.getTime()+size-time)<EPSILON) {
//				if (first.getTime()+size<time) {
				if ((first.getTime()<time-size)&&(first.getTime()+size<time)) {
					return false;
				} else {
					update(time, value);
					this.end = time;
				}
			}
			return true;
		}

		private void update(double time, R value) {
			Segment<R> current = last;
			double insertTime = time;
			R aggregatedValue = value;
			while (current != null) {
				R currentValue = current.getValue();
				R newValue = aggregator.apply(currentValue,aggregatedValue);
				if (currentValue.equals(newValue)) {
					last = current.addAfter(insertTime, aggregatedValue);
					return ;
				} else {
					insertTime =  current.getTime();
					aggregatedValue = newValue;
					current = current.getPrevious();
				}
			}
			init(insertTime, aggregatedValue);
			end = time;
		}

		private void init(double time, R value) {
			first = new Segment<R>(time,value);
			last = first;
			end = time;
		}

		@Override
		public String toString() {
			if (first == null) {
				return "<>";
			} else {
				return "< "+first.toString()+"-"+last.toString()+":"+end+">";
			}
		}

	}


}