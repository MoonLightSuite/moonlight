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

import eu.quanticol.moonlight.core.algorithms.SpatialAlgorithms;
import eu.quanticol.moonlight.core.algorithms.SpatialOperator;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.offline.signal.ParallelSignalCursor;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.online.signal.Update;
import eu.quanticol.moonlight.util.Pair;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

import static eu.quanticol.moonlight.core.algorithms.SpatialAlgorithms.escape;

/**
 * Algorithm for Somewhere and Everywhere Computation
 */
public class SpatialOperators<S, R> extends SpatialOperator<Double, S, R> {
    private SpatialTemporalSignal<R> toReturn;

    public SpatialOperators(LocationService<Double, S> l,
                            Function<SpatialModel<S>, DistanceStructure<S, ?>> distance,
                            BiFunction<IntFunction<R>,
                                    DistanceStructure<S, ?>,
                                    List<R>> operator) {
        super(l, distance, operator);
    }

    public SpatialTemporalSignal<R> computeUnarySpatialOperator(
            SpatialTemporalSignal<R> s)
    {
        if (locSvc.isEmpty()) {
            return outputInit(s.getNumberOfLocations());
        }

        ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
        Iterator<Pair<Double, SpatialModel<S>>> locSvcIterator = locSvc.times();

        double start = cursor.getTime();
        seekSpace(start, null, locSvcIterator);

        return unaryOperator(cursor, start,
                      locSvcIterator);
    }

    private SpatialTemporalSignal<R> outputInit(int locations) {
        return new SpatialTemporalSignal<>(locations);
    }

    private SpatialTemporalSignal<R> unaryOperator(
            ParallelSignalCursor<R> cursor,
            double time,
            Iterator<Pair<Double, SpatialModel<S>>> locSvcIterator)
    {
        //Loop invariant: (current.getFirst() <= time) &&
        //                ((next == null) || (time < next.getFirst()))
        SpatialModel<S> sm = currSpace.getSecond();
        DistanceStructure<S, ?> f = dist.apply(sm);

        toReturn = outputInit(sm.size());

        while (!cursor.completed() && !Double.isNaN(time)) {
            IntFunction<R> spatialSignal = cursor.getValue();
            addResult(time, null, op.apply(spatialSignal, f));
            double nextTime = cursor.forward();

            while ((nextSpace != null) && (nextSpace.getFirst() < nextTime)) {
                currSpace = nextSpace;
                time = currSpace.getFirst();
                nextSpace = getNext(locSvcIterator);
                f = dist.apply(currSpace.getSecond());
                addResult(time, null, op.apply(spatialSignal, f));
            }
            time = nextTime;
            if ((nextSpace != null) && (nextSpace.getFirst() == time)) {
                currSpace = nextSpace;
                dist.apply(currSpace.getSecond());
                nextSpace = getNext(locSvcIterator);
            }
        }
        //TODO: Manage end of signal!
        return toReturn;
    }

    @Override
    protected void addResult(Double start, Double end, List<R> value) {
        toReturn.add(start, value);
    }


    public SpatialTemporalSignal<R> computeDynamic(
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
            List<R> values =  SpatialAlgorithms.reach(domain, spatialSignal1, spatialSignal2, f);
            toReturn.add(time, (values::get));
            double nextTime = Math.min(c1.nextTime(), c2.nextTime());
            c1.move(nextTime);
            c2.move(nextTime);
            while ((next != null) && (next.getFirst() < nextTime)) {
                current = next;
                time = current.getFirst();
                next = getNext(locSvcIterator);
                f = distance.apply(current.getSecond());
                values =  SpatialAlgorithms.reach(domain, spatialSignal1, spatialSignal2, f);
                toReturn.add(time, escape(domain, (values::get), f));
            }
            time = nextTime;
            if ((next != null) && (next.getFirst() == time)) {
                current = next;
                f = distance.apply(current.getSecond());
                next = (locSvcIterator.hasNext() ? locSvcIterator.next() : null);
            }
        }
        return toReturn;
    }
}
