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

package eu.quanticol.moonlight.online.algorithms;

import eu.quanticol.moonlight.online.signal.*;

import java.io.Serializable;
import java.util.function.BiPredicate;

/**
 * Algorithms for basic signal primitives, precisely
 * <ul>
 *   <li> {@link #refine} for refining a signal given an update</li>
 *   <li> {@link #refineChain} for refining a signal
 *        given a sequence of updates
 *   </li>
 *   <li> {@link #select} for selecting a fragment of a signal
 *        given some bounds
 *   </li>
 * </ul>
 */
public class Signals {
    private Signals() {}     // hidden constructor

    public static <V> boolean refineChain(TimeChain<Double, V> s,
                                          TimeChain<Double, V> updates,
                                          BiPredicate<V, V> refinable)
    {
        ChainIterator<Sample<Double, V>> utr = updates.chainIterator();
        ChainIterator<Sample<Double, V>> itr = s.chainIterator();
        Sample<Double, V> current = itr.next();
        V prevV = current.getValue();   // Default when no previous value exists

        if(utr.hasNext()) {
            Update<Double, V> u = nextUpdate(utr, updates.getEnd());

            while (true) {
                if (stillRefining(itr, current, u, refinable, prevV, s.getEnd()))
                {
                    // Save the "next" as the new "current".
                    prevV = current.getValue();
                    current = itr.next();
                } else if (utr.hasNext()) {
                    u = nextUpdate(utr, updates.getEnd());
                    current = itr.previous();
                    if (itr.hasPrevious())
                        current = itr.previous();
                    prevV = itr.tryPeekPrevious(current).getValue();
                } else
                    break;
            }

        }

        return itr.noEffects();
    }

    public static <V> boolean refine(TimeChain<Double, V> s,
                                     Update<Double, V> u,
                                     BiPredicate<V, V> refinable)
    {
        ChainIterator<Sample<Double, V>> itr = s.chainIterator();
        Sample<Double, V> current = itr.next();
        V prevV = current.getValue();   // Default when no previous value exists

        while (stillRefining(itr, current, u, refinable, prevV, s.getEnd()))
        {
            // Save the "next" as the new "current".
            prevV = current.getValue();
            current = itr.next();
        }

        return itr.noEffects();
    }

    private static <V> Update<Double, V>
    nextUpdate(ChainIterator<Sample<Double, V>> itr, double end)
    {
        Sample<Double, V> fst = itr.next();
        Double sEnd = itr.hasNext() ? itr.peekNext().getStart() : end;

        return new Update<>(fst.getStart(), sEnd, fst.getValue());
    }

    /**
     * Signals initialization
     *
     * @param itr segment iterator
     * @param curr current segment
     * @param u update data
     * @return <code>true</code> when the update won't affect the signal anymore,
     *         <code>false</code> otherwise.
     */
    private static <V> boolean stillRefining(
            ChainIterator<Sample<Double, V>> itr,
            Sample<Double, V> curr,
            Update<Double, V> u,
            BiPredicate<V, V> refinable,
            V prevV, double lastT)
    {
        // Current segment unpacking
        double t = curr.getStart();
        V v = curr.getValue();
        double tNext = itr.hasNext() ? itr.peekNext().getStart() : lastT;

        // Update unpacking
        double from = u.getStart();
        double to = u.getEnd();
        V vNew = u.getValue();

        return !doRefine(itr, from, to, vNew, t, tNext, v, prevV, refinable);
    }

    /**
     * Refinement logic.
     * @return <code>true</code> when the update has been completely processed.
     */
    public static <V> boolean doRefine(
            ChainIterator<Sample<Double, V>> itr,
            double from, double to, V vNew, // update
            double t, double tNext, V v,    // current segment
             V prevV,                       // previous value
            BiPredicate<V, V> refinable)
    {
        processUpdate(itr, from, to, vNew, t, tNext, v, prevV, refinable);

        // General Sub-case - to < tNext:
        //          This means the current segment contains the end of the
        //          area to update. Therefore, we must add a segment for the
        //          last (not to be changed) part of the segment.
        //          From now on the signal will not change.
        if(to < tNext && t != to) {
            add(itr, to, v);
            return true;
        }

        if(t == from && itr.hasNext() && itr.peekNext().getValue().equals(vNew))
        {
                itr.next();
                remove(itr);
                itr.previous();
        }

        // Case 4 - t  >= to:
        //          The current segment is beyond the update horizon,
        //          from now on, the signal will not change.
        return t >= to;
    }

    private static <V> void processUpdate(
            ChainIterator<Sample<Double, V>> itr,
            double from, double to, V vNew, // update
            double t, double tNext, V v,    // current segment
            V prevV,                       // previous value
            BiPredicate<V, V> refinable)
    {
        // Case 1 - `from` in (t, tNext):
        //          This means the update starts in the current segment
        if(t < from && tNext > from && refinable.test(v, vNew)) {
            add(itr, from, vNew);
        }
        // Case 2 - from  == t:
        //          This means the current segment starts exactly at
        //          update time, therefore, its value must be updated
        if(t == from && !v.equals(vNew)) {
            update(itr, t, v, vNew, refinable, prevV);
        }

        // Case 3 - t  in (from, to):
        //          This means the current segment starts within the update
        //          horizon and must therefore be updated
        if(t > from && t < to && refinable.test(v, vNew) && !v.equals(vNew)) {
            remove(itr);
        }
    }

    /**
     * Method for checking whether the provided interval refines the current
     * one, and to update it accordingly.
     * @param itr iterator of the signal segments
     * @param t current time instant
     * @param v current value
     * @param vNew new value from the update
     * @param refinable boolean predicate that tells whether it is refinable
     * @param prevV the value of the previous segment of the chain
     */
    private static <V>
    void update(ChainIterator<Sample<Double, V>> itr,
                double t, V v, V vNew, BiPredicate<V, V> refinable, V prevV)
    {
        boolean isRefinable = refinable.test(v, vNew);
        if(isRefinable && prevV.equals(vNew)) {
            remove(itr);
        } else if (isRefinable) {
            Sample<Double, V> s = new TimeSegment<>(t, vNew);
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
    private static <V>
    void remove(ChainIterator<Sample<Double, V>> itr)
    {
        itr.previous();
        itr.remove();
    }

    private static <V> void add(ChainIterator<Sample<Double, V>> itr,
                                Double start, V vNew)
    {
        if(itr.hasPrevious() && !itr.peekPrevious().getValue().equals(vNew)) {
            itr.add(new TimeSegment<>(start, vNew));
        }

        if(itr.hasNext() && itr.peekNext().getValue().equals(vNew)) {
            itr.next();
            remove(itr);
            if(itr.hasPrevious())
                itr.previous();
        }
    }


    /**
     * Selects a fragment of the given {@code TimeChain}
     * @param segments original chain of segments
     * @param from first time of interest for the caller
     * @param to last time of interest for the caller
     * @param <T> Time domain of the chain
     * @param <V> value domain of the chain
     * @return a sub-chain of the input signal
     */
    public static <T extends Comparable<T> & Serializable, V>
    TimeChain<T, V> select(TimeChain<T, V> segments, T from, T to)
    {
        if(from.compareTo(to) > 0)
            throw new UnsupportedOperationException("Illegal selection span");

        int start = 0;
        int end = 1;

        ChainIterator<Sample<T, V>> itr = segments.chainIterator();

        do {
            Sample<T, V> current = itr.next();

            // We went too far, the last returned is the last one useful
            if(current.getStart().compareTo(to) > 0) {
                end = itr.previousIndex();
                break;
            }

            // current is before/at `from`, so it's the last useful index
            if(current.getStart().compareTo(from) <= 0)
                start = itr.previousIndex();

            // Last segment, this is necessarily the last interesting one.
            if(itr.tryPeekNext(current).equals(current))
                end = itr.previousIndex() + 1;

        } while(itr.hasNext());

        return segments.subChain(start, end, to);
    }
}
