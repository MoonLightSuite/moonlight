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

package eu.quanticol.moonlight.offline.algorithms;

import java.util.function.BinaryOperator;

import eu.quanticol.moonlight.offline.signal.Segment;
import eu.quanticol.moonlight.offline.signal.Signal;
import eu.quanticol.moonlight.offline.signal.SignalCursor;

/**
 * Core of the temporal operators.
 *
 *
 * Alternative implementations (e.g. online version) might simply
 * override the {@link #apply(Signal)} method.
 *
 * For the original sliding window algorithm from Lemire:
 * https://dl.acm.org/doi/10.5555/1324123.1324129
 *
 * Note that, except for methods explicitly marked by
 * <i>DIRECTION-AWARE METHOD</i>
 * (which use the {@code isFuture} field to understand the direction),
 * the algorithm is agnostic on the direction of the sliding.
 *
 * @see Window for the internal representation of the Window
 * @see SignalCursor for details on how the signal is scanned
 */
public class SlidingWindow<R> {
	private final double a;
	private final double size;
	private final boolean isFuture;
	
	/**
	 * A valid aggregator must be a binary operator 'f' that is:
	 * - commutative: f(a,b) = f(b,a)
	 * - idempotent in one of the two arguments:
	 * 				for any a,b: f(a,b) = a || f(a,b) = b
	 * TODO: this is not what `idempotent' means
	 *
	 * TODO: instead of specifying valid aggregators here,
	 * 		 we should develop an interface that enforces these constraints.
	 */
	private final  BinaryOperator<R> aggregator;

	/**
	 * Constructs a Sliding Window on the given aggregator and time interval.
	 * @param a beginning of the interval of interest
	 * @param b ending of the interval of interest
	 * @param aggregator the aggregation function the Sliding Window will use
	 * @param isFuture flag to tell whether the direction of the sliding
	 */
	public SlidingWindow(double a, double b,
						 BinaryOperator<R> aggregator,
						 boolean isFuture)
	{
		this.a = a;
		this.size = b - a;
		this.aggregator = aggregator;
		this.isFuture = isFuture;
	}

	/**
	 * Activates the actual shift of the Signal
	 * @param s the Signal to be shifted
	 * @return the shifted Signal
	 */
	public Signal<R> apply(Signal<R> s) {
		// If the signal is empty or shorter than the time horizon,
		// we return an empty signal
		// NOTE: this assumes offline usage (i.e. the signal is complete)
		if (s.isEmpty() || (s.end() - s.start() < size)) {
			return new Signal<>();
		}

		// We prepare the Sliding Window
		SignalCursor<R> cursor = iteratorInit(s);
		Window window = new Window();

		// We actually slide the window
		Signal<R> result = doSlide(cursor, window);

		// We store the final value of the window
		storeEnding(result, window);

		return result;
	}

	/**
	 * @return the size (i.e. relative horizon) of the Sliding Window
	 */
	public double size() {
		return size;
	}

	/**
	 * Actual logic of the sliding process
	 * @param iterator signal cursor initialized at the beginning of the window
	 * @param window an empty window
	 * @return the final result of the sliding
	 */
	protected Signal<R> doSlide(SignalCursor<R> iterator, Window window) {
		Signal<R> result = new Signal<>();

		// We loop over all the Segments of the Signal
		while (!iterator.completed()) {
			double time = iterator.time();
			R value = iterator.value();

			// We try to add the segment's starting instant to the window,
			// if we fail, we are exceeding window's limit, so we must shift it.
			// Before doing that, we save the current beginning of the monotonic
			// edge set, as it still is the correct value, up to the
			// previous time instant
			while (!window.tryAdd(time, value)) {
				result.add(timeOf(window.firstTime()), window.firstValue());
				window.shift(time);
			}
			// We go over to the next Segment of the Signal
			iterator.forward();
		}
		registerLastValidTime(iterator, window);

		return result;
	}

	protected void registerLastValidTime(SignalCursor<R> iter, Window wnd) {
		if(iter.completed() && iter.hasPrevious()) {
			double lastTime;
			// We need to get back to the last segment, so that future iteration
			// can continue from here
			iter.revert();
			lastTime = iter.nextTime();
			R lastValue = iter.value();
			wnd.tryAdd(lastTime, lastValue);
		}
	}

	/**
	 * <i>DIRECTION-AWARE METHOD</i>: we add the last value
     * of the window to the results.
	 * @param result output Signal to update
	 * @param window the Sliding Window we used
	 */
	protected void storeEnding(Signal<R> result, Window window) {
		// If we are sliding to the future,
		// we add the beginning of the Sliding Window to the output.
		// On the contrary, if we are sliding to the past,
		// we add the end of the Sliding Window to the output.
		if (isFuture) {
			result.add(timeOf(window.firstTime()), window.firstValue());
		} else {
			result.add(window.end, window.firstValue());
			//TODO: why window.END & window.FIRST?
			// 		this should still be timeOf(window.firstTime())
			//		but perhaps there are some degenerated cases I cannot
			//		think of, where the window doesn't reach the maximum
			//		size, and in which
			//		timeOf(window.firstTime()) =/= window.end
			//		if this is the case, a proper test should be in place
		}
	}

	/**
	 * This method retrieves a Signal Cursor at the beginning of the horizon.
	 * @param signal the Signal from which the cursor will be extracted
	 * @return a SignalCursor starting at the beginning of the horizon
	 */
	protected SignalCursor<R> iteratorInit(Signal<R> signal) {
		SignalCursor<R> iterator = signal.getIterator(true);
		iterator.move(signal.start() + a);
		return iterator;
	}

	/**
	 * <i>DIRECTION-AWARE METHOD</i>: returns the direction and horizon-aware
	 * version of the current time instant.
	 * @param t the time instant of interest
	 * @return the non-relative version of the time instant
	 */
	protected double timeOf(double t) {
		if (isFuture) {
			return t - a;
		} else {
			return t + size;
		}
	}

	/**
	 * This class implements the Monotonic Edge Set of the Sliding Window.
	 *
	 * @see Segment for the primary data structure used both by the
	 * 				Sliding Window and by the Signal.
	 */
	protected class Window {
		private static final double EPSILON = 0.000001;
		private Segment<R> first;
		private Segment<R> last;
		protected double end;

		/**
		 * We shift the window to the given time instant
		 * @param time the time instant required for shifting the window
		 */
		void shift(double time) {
			double nextTime = first.getSegmentEnd();
			// If the first segment of the window has only one time instant,
			// we restart the window at the given time with the previous value
			// TODO: why the previous value? shouldn't we get a new value?
			if (firstTime() == nextTime) {
				init(time - size, first.getValue());
			} else if (nextTime + size > time) {
				// If the current segment goes beyond the current time point,
				// we cut it and just take the part that starts at the current
				// time point
				first = first.splitAt(time - size);
			} else {
				// Otherwise we just remove the first Segment and make
				// the window start from the next one
				first = first.getNext();
				if (first != null) {
					first.setFirst();
				}
			}
		}

		/**
		 * It adds the given value at the given time to the window,
		 * unless it exceeds the window's size, in which case, it returns false.
		 *
		 * @param time time instant to add to the window
		 * @param value value the window will have at that time instant
		 * @return true if the value is added, false if exceeding window's size
		 */
		boolean tryAdd(double time, R value) {
			// If the window is empty, we initialize it
			// at the current time, with the current value
			if (first == null) {
				init(time, value);
			} else {
				// If the first time point of the window exceeds the maximum
				// size of the window w.r.t the current time point, or,
				// if the current time is beyond the maximum size of the window,
				// the window must be shifted before adding new points, and we
				// therefore immediately return to the caller.

				// NOTE: we could also check whether the window has
				// a negligible size and makes any sense,
				// i.e. Math.abs(first.getTime() + size - time) < EPSILON
				if ((firstTime() < time - size)
						&&(firstTime() + size < time)) {
					return false;
				} else {
					// Otherwise, we update the Sliding Window
					update(time, value);
					this.end = time;
				}
			}
			return true;
		}

		/**
		 * @return the first time instant of the Sliding Window
		 */
		double firstTime() {
			return first.getStart();
		}

		/**
		 * @return the value at the first time instant of the Sliding Window
		 */
		R firstValue() {
			return first.getValue();
		}

		/**
		 * @return the current size (i.e. horizon) of the Window
		 */
		double size() {
			return (first == null ? 0.0 : end - firstTime());
		}

		/**
		 * Updates the Sliding Window with the given value at the given time
		 * @param time the time instant to update
		 * @param value the value to add
		 */
		private void update(double time, R value) {
			Segment<R> current = last; //we go to the last segment of the window
			double insertTime = time;
			R aggregatedValue = value;

			// We loop over the segments of the window and start to aggregate:
			// we start from the last segment, and at each iteration
			// we go backward and aggregate, unless either we find a segment
			// where the aggregator doesn't change the value, or the list of
			// segments ends.
			// If the value didn't change, we found the extreme value we were
			// looking for, in this case, we extend the segment,
			// and we end the Sliding Window here.
			while (current != null) {
				R currentValue = current.getValue();
				R newValue = aggregator.apply(currentValue, aggregatedValue);

				// If the new value equals the one of the segment,
				// we just "extend" the current segment and return to the caller
				if (currentValue.equals(newValue)) {
					last = current.addAfter(insertTime, aggregatedValue);
					return;
				} else {
					// Since the value is different, we store it and shift
					// backwards, the next iteration will compare with the
					// aggregated value
					insertTime =  current.getStart();
					aggregatedValue = newValue;
					current = current.getPrevious();
					// We will re-use the window in the future, so we have to
					// update window's ending to the correct one
					last = current;
					end = insertTime;
				}
			}

			// If the loop ends unsuccessfully, we reached a new extreme.
			// In this case, we restart the Sliding Window from here.
			init(insertTime, aggregatedValue);
			end = time;
		}

		/**
		 * Initialization procedure of the Sliding Window:
		 * we add a degenerated segment with a given value
		 * and which is long exactly the provided time instant
		 * @param time the first (and last) time instant
		 * @param value the first (and only) value of the window
		 */
		private void init(double time, R value) {
			first = new Segment<>(time, value);
			last = first;
			end = time;
		}

		
		@Override
		public String toString() {
			if (first == null) {
				return "<>";
			} else {
				return  "< " + first.toString() +
						" - " + last.toString() +
						" : " + end + ">";
			}
		}
	}
}
