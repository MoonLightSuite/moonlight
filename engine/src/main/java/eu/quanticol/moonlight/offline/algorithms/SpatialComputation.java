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

import static eu.quanticol.moonlight.core.algorithms.SpatialAlgorithms.reach;
import static eu.quanticol.moonlight.core.algorithms.SpatialAlgorithms.escape;

import eu.quanticol.moonlight.core.algorithms.SpatialOperator;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.util.Pair;

import eu.quanticol.moonlight.offline.signal.ParallelSignalCursor;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;


/**
 * Algorithm for Somewhere and Everywhere Computation
 */
public class SpatialComputation<S, R> extends SpatialOperator<Double, S, R> {
    private SpatialTemporalSignal<R> toReturn;

    public SpatialComputation(LocationService<Double, S> l,
                              Function<SpatialModel<S>, DistanceStructure<S, ?>> distance,
                              BiFunction<IntFunction<R>,
                                    DistanceStructure<S, ?>,
                                    List<R>> operator) {
        super(l, distance, operator);
    }

    public SpatialTemporalSignal<R> computeUnary(SpatialTemporalSignal<R> s) {
        // Output Init
        outputInit(s.getNumberOfLocations());
        if (locSvc.isEmpty()) {
            return toReturn;
        }

        ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
        double t = cursor.getTime();

        Iterator<Pair<Double, SpatialModel<S>>> spaceItr = shiftSpaceModel(t);

        SpatialModel<S> sm = currSpace.getSecond();
        DistanceStructure<S, ?> f = dist.apply(sm);

        return unaryOperator(cursor, t, f, spaceItr);
    }

    private void outputInit(int locations) {
        toReturn = new SpatialTemporalSignal<>(locations);
    }

    private SpatialTemporalSignal<R> unaryOperator(
            ParallelSignalCursor<R> cursor,
            double t,
            DistanceStructure<S, ?> f,
            Iterator<Pair<Double, SpatialModel<S>>> locSvcIterator)
    {
        while (!cursor.completed() && !Double.isNaN(t)) {
            IntFunction<R> spatialSignal = cursor.getValue();
            double tNext = cursor.forward();
            computeOp(t, tNext, spatialSignal, f, locSvcIterator);
            t = moveSpatialModel(tNext, locSvcIterator);
        }
        //TODO: Manage end of signal!
        return toReturn;
    }

    private void computeOp(double t, double tNext,
                           IntFunction<R> spatialSignal,
                           DistanceStructure<S, ?> f,
                           Iterator<Pair<Double, SpatialModel<S>>> locSvcIterator)
    {
        addResult(t, null, op.apply(spatialSignal, f));

        while (isNextSpaceModelWithinHorizon(tNext)) {
            currSpace = nextSpace;
            t = currSpace.getFirst();
            nextSpace = getNext(locSvcIterator);
            f = dist.apply(currSpace.getSecond());
            addResult(t, null, op.apply(spatialSignal, f));
        }
    }

    private Double moveSpatialModel(
            @NotNull Double t,
            Iterator<Pair<Double, SpatialModel<S>>> locSvcIterator)
    {
        if ((nextSpace != null) && t.equals(nextSpace.getFirst())) {
            currSpace = nextSpace;
            dist.apply(currSpace.getSecond());
            nextSpace = getNext(locSvcIterator);
        }
        return t;
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
        outputInit(s1.getNumberOfLocations());
        if (locSvc.isEmpty()) {
            return toReturn;
        }

        ParallelSignalCursor<R> c1 = s1.getSignalCursor(true);
        ParallelSignalCursor<R> c2 = s2.getSignalCursor(true);
        double t = Math.max(s1.start(), s2.start());

        Iterator<Pair<Double, SpatialModel<S>>> spaceItr = shiftSpaceModel(t);
        SpatialModel<S> sm = currSpace.getSecond();
        DistanceStructure<S, ?> f = distance.apply(sm);

        //Loop invariant: (current.getFirst() <= time) &&
        //                ((next == null) || (time < next.getFirst()))
        c1.move(t);
        c2.move(t);
        while (!c1.completed() && !c2.completed() && !Double.isNaN(t)) {
            IntFunction<R> spatialSignal1 = c1.getValue();
            IntFunction<R> spatialSignal2 = c2.getValue();
            List<R> values = reach(domain, spatialSignal1, spatialSignal2, f);
            toReturn.add(t, (values::get));
            double tNext = Math.min(c1.nextTime(), c2.nextTime());
            c1.move(tNext);
            c2.move(tNext);

            while (isNextSpaceModelWithinHorizon(tNext)) {
                currSpace = nextSpace;
                t = currSpace.getFirst();
                nextSpace = getNext(spaceItr);
                f = distance.apply(currSpace.getSecond());
                values = reach(domain, spatialSignal1, spatialSignal2, f);
                toReturn.add(t, escape(domain, (values::get), f));
            }

            t = tNext;
            if (isNextSpaceModelMeaningful()) {
                currSpace = nextSpace;
                nextSpace = getNext(spaceItr);
                f = distance.apply(currSpace.getSecond());
            }
        }
        return toReturn;
    }
}
