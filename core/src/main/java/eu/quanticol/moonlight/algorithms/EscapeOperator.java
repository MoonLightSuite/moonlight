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

import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.util.Pair;

import java.util.Iterator;
import java.util.function.Function;

/**
 * Algorithm for Escape Operator Computation
 */
public class EscapeOperator {

    private EscapeOperator() {} // Hidden constructor

    public static <S,R> SpatialTemporalSignal<R> computeDynamic(
            LocationService<S> l,
            Function<SpatialModel<S>, DistanceStructure<S, ?>> distance,
            SignalDomain<R> domain,
            SpatialTemporalSignal<R> s)
    {

        SpatialTemporalSignal<R> toReturn =
                new SpatialTemporalSignal<>(s.getNumberOfLocations());

        if (l.isEmpty()) {
            return toReturn;
        }

        ParallelSignalCursor<R> cursor = s.getSignalCursor(true);

        Iterator<Pair<Double, SpatialModel<S>>> locSvcIterator = l.times();
        Pair<Double, SpatialModel<S>> current = locSvcIterator.next();
        Pair<Double, SpatialModel<S>> next = getNext(locSvcIterator);

        double time = cursor.getTime();
        while ((next != null) && (next.getFirst() <= time)) {
            current = next;
            next = getNext(locSvcIterator);
        }

        // Loop invariant: (current.getFirst() <= time) &&
        //                 ((next==null)||(time<next.getFirst()))
        SpatialModel<S> sm = current.getSecond();
        DistanceStructure<S, ?> f = distance.apply(sm);
        while (!cursor.completed() && !Double.isNaN(time)) {
            Function<Integer, R> spatialSignal = cursor.getValue();
            toReturn.add(time, f.escape(domain, spatialSignal));
            double nextTime = cursor.forward();
            while ((next != null) && (next.getFirst() < nextTime)) {
                current = next;
                time = current.getFirst();
                next = getNext(locSvcIterator);
                f = distance.apply(current.getSecond());
                toReturn.add(time, f.escape(domain, spatialSignal));
            }
            time = nextTime;
            current = (next != null ? next : current);
            next = getNext(locSvcIterator);
        }
        //TODO: Manage end of signal!
        return toReturn;
    }

    /**
     * Returns the next element if there is one, otherwise null
     * @param iter Location Service Iterator
     * @param <S> Spatial Domain
     * @return Next element of the Location Service
     */
    private static <S> Pair<Double, SpatialModel<S>> getNext(
            Iterator<Pair<Double, SpatialModel<S>>> iter)
    {
        return (iter.hasNext() ? iter.next() : null);
    }
}
