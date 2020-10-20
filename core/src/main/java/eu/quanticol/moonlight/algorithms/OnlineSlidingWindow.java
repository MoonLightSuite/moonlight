/*
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
 */

package eu.quanticol.moonlight.algorithms;

import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SignalCursor;

import java.util.function.BinaryOperator;

public class OnlineSlidingWindow<R> extends SlidingWindow<R> {
    private final R undefined;
    private final BinaryOperator<R> aggregator;
    private SignalCursor<R> previousCursor;
    private Window previousWindow;
    private final double horizon;
    private final boolean isFuture;

    /**
     * Constructs a Sliding Window on the given aggregator and time interval.
     *
     * @param a          beginning of the interval of interest
     * @param b          ending of the interval of interest
     * @param aggregator the aggregation function the Sliding Window will use
     * @param isFuture   flag to tell whether the direction of the sliding
     */
    public OnlineSlidingWindow(double a, double b,
                               BinaryOperator<R> aggregator,
                               boolean isFuture,
                               R undefined,
                               double horizon)
    {
        super(a, b, aggregator, isFuture);
        this.isFuture = isFuture;
        this.aggregator = aggregator;
        this.undefined = undefined;
        this.horizon = horizon;
    }

    /**
     * Activates the actual shift of the Signal
     * @param s the Signal to be shifted
     * @return the shifted Signal
     */
    @Override
    public Signal<R> apply(Signal<R> s) {
        // We prepare the Sliding Window
        SignalCursor<R> cursor = loadCursor(s);
         Window window = loadWindow();

        // We actually slide the window
        Signal<R> result = doSlide(cursor, window);

        try {
            // We store the final value of the window
            storeEnding(result, window);
        } catch(NullPointerException e) {
            // If we have no results, we slided to an undefined area
            // from the very beginning
            if(result.isEmpty()) {
                Signal<R> o = new Signal<>();
                o.add(s.start(), undefined);
                o.endAt(s.end());
                return o;
            }
        }

        // If the signal is shorter than the time horizon,
        // we return a Signal containing "undefined" information
        if (result.end() < s.end()) {
            double newEnd = result.end();
            result.endAt(Double.NaN);
            result.add(newEnd, undefined);
            result.endAt(s.end());
        }

        return result;
    }

    private Window loadWindow() {
        if(previousWindow ==  null)
            previousWindow = new Window();

        return previousWindow;
    }

    private SignalCursor<R> loadCursor(Signal<R> s) {
        if(previousCursor == null)
            previousCursor = iteratorInit(s);

        return previousCursor;
    }

    /**
     * @see SlidingWindow#storeEnding(Signal, Window)
     */
    @Override
    protected void storeEnding(Signal<R> result, Window window) {
        // If we are sliding to the future,
        // we add the beginning of the Sliding Window to the output.
        // On the contrary, if we are sliding to the past,
        // we add the end of the Sliding Window to the output.


        R value = aggregator.apply(undefined, window.firstValue());
        if (isFuture) {
            result.add(timeOf(window.firstTime()), value);
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
}
