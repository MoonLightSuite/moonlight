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

package eu.quanticol.moonlight.algorithms;

import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.core.space.DefaultDistanceStructure;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.util.Pair;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * Algorithm for Somewhere and Everywhere Computation
 */
public class SpaceOperator {
    private SpaceOperator() {} // Hidden constructor

    public static <S, R> SpatialTemporalSignal<R> computeWhereDynamic(
            LocationService<Double, S> l,
            Function<SpatialModel<S>, DefaultDistanceStructure<S, ?>> distance,
            BiFunction<IntFunction<R>,
                    DefaultDistanceStructure<S, ?>,
                       List<R>> operator,
            SpatialTemporalSignal<R> s)
    {
        SpatialTemporalSignal<R> toReturn = new SpatialTemporalSignal<>(s.
                getNumberOfLocations());
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

        whereOperator(cursor, time, current, next, distance, operator,
                      toReturn, locSvcIterator);

        return toReturn;
    }

    public static <S,R> SpatialTemporalSignal<R> computeEscapeDynamic(
            LocationService<Double, S> l,
            Function<SpatialModel<S>, DefaultDistanceStructure<S, ?>> distance,
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

        escapeOperator(cursor, time, current, next, distance, domain, toReturn,
                locSvcIterator);

        return toReturn;
    }

    private static <S, R> SpatialTemporalSignal<R> whereOperator(
            ParallelSignalCursor<R> cursor,
            double time,
            Pair<Double, SpatialModel<S>> current,
            Pair<Double, SpatialModel<S>> next,
            Function<SpatialModel<S>, DefaultDistanceStructure<S, ?>> distance,
            BiFunction<IntFunction<R>, DefaultDistanceStructure<S, ?>, List<R>> operator,
            SpatialTemporalSignal<R> toReturn,
            Iterator<Pair<Double, SpatialModel<S>>> locSvcIterator)
    {
        //Loop invariant: (current.getFirst() <= time) &&
        //                ((next == null) || (time < next.getFirst()))
        while (!cursor.completed() && !Double.isNaN(time)) {
            IntFunction<R> spatialSignal = cursor.getValue();
            SpatialModel<S> sm = current.getSecond();
            DefaultDistanceStructure<S, ?> f = distance.apply(sm);
            toReturn.add(time, operator.apply(spatialSignal, f));
            double nextTime = cursor.forward();
            while ((next != null) && (next.getFirst() < nextTime)) {
                current = next;
                time = current.getFirst();
                next = getNext(locSvcIterator);
                f = distance.apply(current.getSecond());
                toReturn.add(time, operator.apply(spatialSignal, f));
            }
            time = nextTime;
            if ((next != null) && (next.getFirst() == time)) {
                current = next;
                f = distance.apply(current.getSecond());
                next = (locSvcIterator.hasNext() ? locSvcIterator.next() : null);
            }
        }
        //TODO: Manage end of signal!
        return toReturn;
    }

    private static <S, R> SpatialTemporalSignal<R> escapeOperator(
            ParallelSignalCursor<R> cursor,
            double time,
            Pair<Double, SpatialModel<S>> current,
            Pair<Double, SpatialModel<S>> next,
            Function<SpatialModel<S>, DefaultDistanceStructure<S, ?>> distance,
            SignalDomain<R> domain,
            SpatialTemporalSignal<R> toReturn,
            Iterator<Pair<Double, SpatialModel<S>>> locSvcIterator)
    {
        // Loop invariant: (current.getFirst() <= time) &&
        //                 ((next==null)||(time<next.getFirst()))
        SpatialModel<S> sm = current.getSecond();
        DefaultDistanceStructure<S, ?> f = distance.apply(sm);
        while (!cursor.completed() && !Double.isNaN(time)) {
            IntFunction<R> spatialSignal = cursor.getValue();
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
            if ((next != null) && (next.getFirst() == time)) {
                current = next;
                f = distance.apply(current.getSecond());
                next = (locSvcIterator.hasNext() ? locSvcIterator.next() : null);
            }
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
