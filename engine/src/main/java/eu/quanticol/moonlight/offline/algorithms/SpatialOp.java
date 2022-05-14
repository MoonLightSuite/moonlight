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

import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpaceIterator;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.offline.signal.ParallelSignalCursor;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

import static eu.quanticol.moonlight.core.algorithms.SpatialAlgorithms.reach;

/**
 * Algorithm for Somewhere and Everywhere Computation
 */
public class SpatialOp<S, R> {
    private final SpaceIterator<Double, S> spaceItr;
    private final BiFunction<IntFunction<R>, DistanceStructure<S, ?>, IntFunction<R>> op;
    ParallelSignalCursor<R> cursor;
    private SpatialTemporalSignal<R> result;

    public SpatialOp(
            LocationService<Double, S> l,
            Function<SpatialModel<S>, DistanceStructure<S, ?>> distance,
            BiFunction<IntFunction<R>, DistanceStructure<S, ?>,
                    IntFunction<R>> operator) {
        this.op = operator;
        spaceItr = new SpaceIterator<>(l, distance);
    }

    @SafeVarargs
    private static <C> boolean isNotCompleted(ParallelSignalCursor<C>... cursors) {
        return
                Arrays.stream(cursors)
                        .map(c -> !c.isCompleted())
                        .reduce(true, (c1, c2) -> c1 && c2);
    }

    public SpatialTemporalSignal<R> computeUnary(SpatialTemporalSignal<R> s) {
        outputInit(s.getNumberOfLocations());
        if (!spaceItr.isLocationServiceEmpty()) {
            doCompute(s);
        }
        return result;
    }

    private void outputInit(int locations) {
        result = new SpatialTemporalSignal<>(locations);
    }

    private void doCompute(SpatialTemporalSignal<R> s) {
        cursor = s.getSignalCursor(true);
        double t = cursor.getCurrentTime();
        spaceItr.init(t);
        // addResult(space.getFirst(), space.getSecond());

        while (!Double.isNaN(t) && isNotCompleted(cursor)) {
            DistanceStructure<S, ?> ds = spaceItr.generateDistanceStructure();
            IntFunction<R> spatialSignal = cursor.getCurrentValue();
            double tNext = cursor.forwardTime();
            addResult(t, tNext, op.apply(spatialSignal, ds));
            spaceItr.forEach(tNext, (itT, itDs) ->
                    addResult(itT, tNext, op.apply(spatialSignal, itDs))
            );
            t = moveSpatialModel(tNext);
        }
    }

    private Double moveSpatialModel(@NotNull Double t) {
        if (spaceItr.isNextSpaceModelAtSameTime(t)) {
            spaceItr.shiftSpatialModel();
        }
        return t;
    }

    protected void addResult(Double start, Double end, IntFunction<R> value) {
        result.add(start, value);
    }


    public SpatialTemporalSignal<R> computeBinary(SpatialTemporalSignal<R> s) {
        outputInit(s.getNumberOfLocations());
        if (!spaceItr.isLocationServiceEmpty()) {
            doCompute(s);
        }
        return result;
    }


    public SpatialTemporalSignal<R> computeDynamic(
            SignalDomain<R> domain,
            SpatialTemporalSignal<R> s1,
            SpatialTemporalSignal<R> s2) {
        outputInit(s1.getNumberOfLocations());
        if (!spaceItr.isLocationServiceEmpty()) {
            ParallelSignalCursor<R> c1 = s1.getSignalCursor(true);
            ParallelSignalCursor<R> c2 = s2.getSignalCursor(true);
            double t = Math.max(s1.start(), s2.start());

            spaceItr.init(t);
            DistanceStructure<S, ?> f = spaceItr.generateDistanceStructure();

            //Loop invariant: (current.getFirst() <= time) &&
            //                ((next == null) || (time < next.getFirst()))
            c1.move(t);
            c2.move(t);
            while (!Double.isNaN(t) && isNotCompleted(c1, c2)) {
                IntFunction<R> spatialSignal1 = c1.getCurrentValue();
                IntFunction<R> spatialSignal2 = c2.getCurrentValue();

                IntFunction<R> values = reach(domain, spatialSignal1, spatialSignal2,
                        f);
                result.add(t, values);
                double tNext = Math.min(c1.nextTime(), c2.nextTime());
                c1.move(tNext);
                c2.move(tNext);

                while (spaceItr.isNextSpaceModelWithinHorizon(tNext)) {
                    spaceItr.shiftSpatialModel();
                    t = spaceItr.getCurrentT();
                    f = spaceItr.generateDistanceStructure();
                    values = reach(domain, spatialSignal1, spatialSignal2, f);
                    //result.add(t, escape(domain, values, f));
                    result.add(t, values);
                }

                t = tNext;
                if (spaceItr.isNextSpaceModelMeaningful()) {
                    spaceItr.shiftSpatialModel();
                    f = spaceItr.generateDistanceStructure();
                }
            }
        }
        return result;
    }
}
