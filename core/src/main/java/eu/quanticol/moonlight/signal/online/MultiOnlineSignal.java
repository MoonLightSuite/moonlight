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

import eu.quanticol.moonlight.algorithms.online.Refinement;
import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.SignalDomain;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Class to represent n-dimensional online time signals.
 */
public class MultiOnlineSignal
        implements TimeSignal<Double, List<AbstractInterval<?>>>
{
    private final TimeChain<Double, List<AbstractInterval<?>>> segments;

    /**
     * @param domain The signal domain to consider
     */
    public MultiOnlineSignal(SignalDomain<List<AbstractInterval<?>>> domain) {
        this.segments = new TimeChain<>(Double.POSITIVE_INFINITY);
        this.segments.add(new ImmutableSegment<>(0.0, domain.any()));
    }

    /**
     * @return the internal list of segments;
     */
    @Override
    public TimeChain<Double, List<AbstractInterval<?>>> getSegments() {
        return segments;
    }

    /**
     * @return the time point where the signal starts.
     */
    public double getStart() {
        assert segments.peekFirst() != null;
        return segments.peekFirst().getStart();
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
    public boolean
        refine(Update<Double, List<AbstractInterval<?>>> u)
    {
        //TODO: should handle the case where the update is
        //      a list of a different size
        return Refinement.refine(segments, u,
                (v, vNew) -> IntStream.range(0, v.size()).parallel()
                                      .filter(i -> !v.get(i)
                                                     .contains(vNew.get(i)))
                                      .boxed().count() != 0);
    }


    @Override
    public TimeChain<Double, List<AbstractInterval<?>>> select(Double from,
                                                               Double to)
    {
        int start = 0;
        int end = 1;

        DiffIterator<SegmentInterface<Double, List<AbstractInterval<?>>>> itr =
                segments.diffIterator();
        SegmentInterface<Double, List<AbstractInterval<?>>> current;

        while (itr.hasNext()) {
            current = itr.next();
            if (current.getStart() > from && start == 0) {
                start = itr.previousIndex();
            }
            if(current.getStart() > to) {
                end = itr.previousIndex();
                break;
            }
        }

        return segments.subChain(start, end, to);
    }

    @Override
    public String toString() {
        return "OnlineSignal{" + "segments=" + segments + "}";
    }




}
