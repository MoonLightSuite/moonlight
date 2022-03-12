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

import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.offline.signal.ParallelSignalCursor;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.core.base.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

import static eu.quanticol.moonlight.core.algorithms.SpatialAlgorithms.escape;
import static eu.quanticol.moonlight.core.algorithms.SpatialAlgorithms.reach;

/**
 * Algorithm for Reach Operator Computation
 */
public class ReachOperator
        <T extends Comparable<T> & Serializable, S, R extends Comparable<R>>
{
    private final LocationService<T, S> locSvc;
    private final Function<SpatialModel<S>, DistanceStructure<S, ?>> dist;
    /*private final BiFunction<Function<Integer, R>,
            DistanceStructure<S, ?>,
            List<R>> op;*/

    Iterator<Pair<T, SpatialModel<S>>> spaceItr;
    Pair<T, SpatialModel<S>> currSpace;
    Pair<T, SpatialModel<S>> nextSpace;

    public ReachOperator(@NotNull LocationService<T, S> locationService,
                         Function<SpatialModel<S>,
                                  DistanceStructure<S, ?>> distance,
                         SignalDomain<R> domain,
                         SpatialTemporalSignal<R> s1,
                         SpatialTemporalSignal<R> s2)
    {
        checkLocationServiceValidity(locationService);
        locSvc = locationService;
        dist = distance;
    }

    private void checkLocationServiceValidity(LocationService<T, S> locSvc)
    {
        if (locSvc.isEmpty())
            throw new UnsupportedOperationException("The location Service " +
                    "must not be empty!");
    }

    //TODO: same as offline
    public static <S, R> SpatialTemporalSignal<R> computeDynamic(
            LocationService<Double, S> locSvc,
            Function<SpatialModel<S>, DistanceStructure<S, ?>> distance,
            SignalDomain<R> domain,
            SpatialTemporalSignal<R> s1,
            SpatialTemporalSignal<R> s2)
    {
        SpatialTemporalSignal<R> toReturn =
                new SpatialTemporalSignal<>(s1.getNumberOfLocations());

        if (locSvc.isEmpty()) {
            return toReturn;
        }

        ParallelSignalCursor<R> c1 = s1.getSignalCursor(true);
        ParallelSignalCursor<R> c2 = s2.getSignalCursor(true);
        Iterator<Pair<Double, SpatialModel<S>>> locSvcIterator = locSvc.times();
        Pair<Double, SpatialModel<S>> current = locSvcIterator.next();
        Pair<Double, SpatialModel<S>> next = getNext(locSvcIterator);

        double time = Math.max(s1.start(), s2.start());
        while ((next != null) && (next.getFirst() <= time)) {
            current = next;
            next = getNext(locSvcIterator);
        }

        //Loop invariant: (current.getFirst() <= time) &&
        //                ((next == null) || (time < next.getFirst()))
        c1.move(time);
        c2.move(time);
        SpatialModel<S> sm = current.getSecond();
        DistanceStructure<S, ?> f = distance.apply(sm);
        while (!c1.completed() && !c2.completed() && !Double.isNaN(time)) {
            IntFunction<R> spatialSignal1 = c1.getValue();
            IntFunction<R> spatialSignal2 = c2.getValue();
            List<R> values =  reach(domain, spatialSignal1, spatialSignal2, f);
            toReturn.add(time, (values::get));
            double nextTime = Math.min(c1.nextTime(), c2.nextTime());
            c1.move(nextTime);
            c2.move(nextTime);
            while ((next != null) && (next.getFirst() < nextTime)) {
                current = next;
                time = current.getFirst();
                next = getNext(locSvcIterator);
                f = distance.apply(current.getSecond());
                values =  reach(domain, spatialSignal1, spatialSignal2, f);
                toReturn.add(time, escape(domain,(values::get), f));
            }
            time = nextTime;
            current = (next != null ? next : current);
            next = getNext(locSvcIterator);
        }
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
