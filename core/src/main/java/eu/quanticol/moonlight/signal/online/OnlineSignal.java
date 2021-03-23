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

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.SignalDomain;

import java.util.NoSuchElementException;

/**
 * @deprecated replaced by the more general {@link MultiOnlineSignal}
 * Class to represent online signals. Work in progress
 * @param <D> Signal domain of interest
 */
@Deprecated
public class OnlineSignal<D extends Comparable<D>>
        implements SignalInterface<Double, AbstractInterval<D>> {
    private final SegmentChain<Double, AbstractInterval<D>> segments;


    /**
     * @param domain The signal domain to consider
     */
    public OnlineSignal(SignalDomain<D> domain) {
        this.segments = new SegmentChain<>(Double.POSITIVE_INFINITY);
        this.segments.add(new ImmutableSegment<>(0.0, new AbstractInterval<>(domain.min(), domain.max())));
    }

    /**
     * //TODO: hide this
     * @return the internal list of segments;
     */
    public SegmentChain<Double, AbstractInterval<D>> getSegments() {
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
        refine(Update<Double, AbstractInterval<D>> u)
    {
        if(u.getStart() > u.getEnd()) {
            throw new IllegalArgumentException("Invalid update time span");
        }
        DiffIterator<SegmentInterface<Double, AbstractInterval<D>>> itr =
                segments.diffIterator();
        SegmentInterface<Double, AbstractInterval<D>> current = itr.next();

        boolean done = false;

        while (itr.hasNext()) {
            if(doRefine(itr, current, u.getStart(), u.getEnd(), u.getValue())) {
                done = true;
                break;
            }

            // Save the "next" as the next "current".
            current = itr.next();
        }

        if(!done) // To handle single-segment signals
            doRefine(itr, current, u.getStart(), u.getEnd(), u.getValue());

        return !itr.getChanges().isEmpty();
    }


    /**
     * Refinement logic
     *
     * @param itr segment iterator
     * @param curr current segment
     * @param from starting time of the update
     * @param to ending time of the update
     * @param vNew new value of the update
     * @return true when the update won't affect the signal anymore,
     *         false otherwise.
     */
    private boolean doRefine(
            DiffIterator<SegmentInterface<Double, AbstractInterval<D>>> itr,
            SegmentInterface<Double, AbstractInterval<D>> curr,
            double from, double to, AbstractInterval<D> vNew)
    {
        double t = curr.getStart();
        double tNext = Double.POSITIVE_INFINITY;
        try {
            tNext = itr.peekNext().getStart();
        } catch(NoSuchElementException ignored) {
            // Exception handled by default value of tNext
        }

        AbstractInterval<D> v = curr.getValue();

        // Case 1 - `from` in (t, tNext):
        //          This means the update starts in the current segment
        if(t < from && tNext > from) {
            add(itr, from, vNew);
            return false;
        }
        // Case 2 - from  == t:
        //          This means the current segment starts exactly at
        //          update time, therefore, its value must be updated
        if(t == from) {
            update(itr, t, v, vNew);
        }

        // Case 3 - t  in (from, to):
        //          This means the current segment starts within the update
        //          horizon and must therefore be updated
        if(t > from && t < to) {
            remove(itr);
        }

        // General Sub-case - to < tNext:
        //          This means the current segment contains the end of the
        //          area to update. Therefore, we must add a segment for the
        //          last part of the segment.
        //          From now on the signal will not change.
        if(to < tNext && t != to) {
            add(itr, to, v);
            return true;
        }

        // Case 4 - t  >= to:
        //          The current segment is beyond the update horizon,
        //          from now on, the signal will not change.
        return t >= to;
    }

    /**
     * Method for checking whether the provided interval refines the current
     * one, and to update it accordingly.
     * @param itr iterator of the signal segments
     * @param t current time instant
     * @param v current value
     * @param vNew new value from the update
     */
    private void update(
            DiffIterator<SegmentInterface<Double, AbstractInterval<D>>> itr,
            double t, AbstractInterval<D> v,
            AbstractInterval<D> vNew)
    {
        if(v.contains(vNew)) {
            ImmutableSegment<AbstractInterval<D>> s =
                                                new ImmutableSegment<>(t, vNew);

            SegmentInterface<Double, AbstractInterval<D>> p = itr.peekPrevious();

            if(!s.equals(p))
                itr.set(s);
        } else {
            throw new UnsupportedOperationException("Refining interval: " +
                                                    vNew + " is wider than " +
                                                    "the original:" + v);
        }
    }

    /**
     * Removes the last object seen by the iterator.
     * Note that the iterator is one step ahead, so we have to bring it back
     * first.
     * @param itr iterator to update
     */
    private void remove(
            DiffIterator<SegmentInterface<Double, AbstractInterval<D>>> itr)
    {
        itr.previous();
        itr.remove();
    }

    private void add(
            DiffIterator<SegmentInterface<Double, AbstractInterval<D>>> itr,
            Double start, AbstractInterval<D> value)
    {
        if(!itr.peekPrevious().getValue().equals(value))
            itr.add(new ImmutableSegment<>(start, value));
    }

    /**
     * Returns the interval of valid signal values at time <code>t</code>. An {@link IllegalArgumentException}
     * is thrown whenever the value <code>t</code> is outside the signal time boundaries.
     *
     * @param t time instant.
     * @return the interval of valid signal values at time <code>t</code>.
     */
    public AbstractInterval<D> getValueAt(Double t) {
        DiffIterator<SegmentInterface<Double, AbstractInterval<D>>> itr =
                segments.diffIterator();
        SegmentInterface<Double, AbstractInterval<D>> current = null;

        while (itr.hasNext()) {
            current = itr.next();
            if (current.getStart() > t) {
                // We went too far, we have to look at the previous element
                // So we have to move the iterator twice back
                // (as we are now looking backwards)
                itr.previous();
                return itr.previous().getValue();
            }
        }

        if(current != null) // Single-segment signal
            return current.getValue();
        else
            throw new UnsupportedOperationException("Empty signal provided");
    }

    @Override
    public SegmentChain<Double, AbstractInterval<D>> select(Double from,
                                                            Double to)
    {
        int start = 0;
        int end = 1;

        DiffIterator<SegmentInterface<Double, AbstractInterval<D>>> itr =
                segments.diffIterator();
        SegmentInterface<Double, AbstractInterval<D>> current;

        do{
            current = itr.next();
            if(current.getStart() > to) {
                end = itr.previousIndex();
                break;
            }
            if (current.getStart() > from) {
                start = itr.previousIndex();
            }
            if(itr.tryPeekNext(current).equals(current)) {
                start = itr.previousIndex();
                end = itr.previousIndex() + 1;
            }
        } while(itr.hasNext());

        return segments.subChain(start, end, Math.max(to, current.getStart()));
    }

    @Override
    public String toString() {
        return "OnlineSignal{" + "segments=" + segments + "}";
    }




}
