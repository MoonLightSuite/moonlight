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

package eu.quanticol.moonlight.signal.online;

import eu.quanticol.moonlight.algorithms.online.Signals;
import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.SignalDomain;

/**
 * Class to represent 1-dimensional online time signals.
 * @param <D> Signal domain of interest
 */
public class OnlineSignal<D extends Comparable<D>>
        implements TimeSignal<Double, AbstractInterval<D>>
{
    private final TimeChain<Double, AbstractInterval<D>> segments;

    /**
     * @param domain The signal domain to consider
     */
    public OnlineSignal(SignalDomain<D> domain) {
        AbstractInterval<D> i = new AbstractInterval<>(domain.min(), domain.max());
        this.segments = new TimeChain<>(new TimeSegment<>(0.0, i), Double.POSITIVE_INFINITY);
    }

    /**
     * @param defaultValue The signal default value to consider
     */
    public OnlineSignal(AbstractInterval<D> defaultValue)
    {
        this.segments = new TimeChain<>(new TimeSegment<>(0.0, defaultValue), Double.POSITIVE_INFINITY);
    }

    /**
     * @return the internal list of segments;
     */
    @Override
    public TimeChain<Double, AbstractInterval<D>> getSegments() {
        return segments;
    }

    /**
     * @return the time point where the signal starts.
     */
    public double getStart() {
        assert segments.getFirst() != null;
        return segments.getFirst().getStart();
    }

    /**
     * Refines the current signal given the argument's update data by setting
     * the signal <em>value</em> in the time interval:
     * [<code>u.getStart()</code>, <code>u.getEnd()</code>).
     *
     * <p>
     * An {@link IllegalArgumentException} is thrown whenever the value
     * <code>value</code> is not in the current intervals
     * in the time interval <code>[from,to)</code>.
     *
     * <p>
     * Requires: <code>!segments.isEmpty()</code>
     *
     * @param u update data, containing the time instants at which the update
     *          starts and ends, and the new value.
     * @return the list of updated segments.
     * @see Update
     */
    @Override
    public boolean refine(Update<Double, AbstractInterval<D>> u) {
        return Signals.refine(segments, u, AbstractInterval::contains);
    }

    @Override
    public boolean refine(TimeChain<Double, AbstractInterval<D>> updates) {
        return Signals.refineChain(segments, updates, AbstractInterval::contains);
    }


    @Override
    public TimeChain<Double, AbstractInterval<D>> select(Double from, Double to)
    {
        return Signals.select(segments, from, to);
    }

    @Override
    public String toString() {
        return "OnlineSignal{" + "segments=" + segments + "}";
    }




}
