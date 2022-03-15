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

import eu.quanticol.moonlight.core.algorithms.SpaceIterator;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;

import eu.quanticol.moonlight.offline.signal.ParallelSignalCursor;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * Algorithm for Somewhere and Everywhere Computation
 */
public class SpatialComputation<S, R> {
    private final SpaceIterator<Double, S, R> spaceItr;
    private SpatialTemporalSignal<R> result;
    ParallelSignalCursor<R> cursor;

    public SpatialComputation(
            LocationService<Double, S> l,
            Function<SpatialModel<S>, DistanceStructure<S, ?>> distance,
            BiFunction<IntFunction<R>, DistanceStructure<S, ?>,
                    List<R>> operator)
    {
        spaceItr = new SpaceIterator<>(l, distance, operator);
    }

    public SpatialTemporalSignal<R> computeUnary(SpatialTemporalSignal<R> s) {
        outputInit(s);
        if (!spaceItr.isLocationServiceEmpty()) {
            cursor = s.getSignalCursor(true);
            doCompute();
        }
        return result;
    }

    private void outputInit(SpatialTemporalSignal<R> s) {
        result = new SpatialTemporalSignal<>(s.getNumberOfLocations());
    }

    private void doCompute() {
        double t = cursor.getTime();
        spaceItr.init(t, this::addResult);
        DistanceStructure<S, ?> f = spaceItr.generateDistanceStructure();

        while (isNotCompleted(t, cursor)) {
            IntFunction<R> spatialSignal = cursor.getValue();
            double tNext = cursor.forward();
            spaceItr.computeOp(t, tNext, f, spatialSignal);
            t = moveSpatialModel(tNext);
        }
    }

    private Double moveSpatialModel(@NotNull Double t) {
        if (spaceItr.isNextSpaceModelAtSameTime(t)) {
            spaceItr.shiftSpatialModel();
        }
        return t;
    }

    protected void addResult(Double start, Double end, List<R> value) {
        result.add(start, value);
    }

    public SpatialTemporalSignal<R> computeDynamic(
            SignalDomain<R> domain,
            SpatialTemporalSignal<R> s1,
            SpatialTemporalSignal<R> s2)
    {
        outputInit(s1);
        if (!spaceItr.isLocationServiceEmpty()) {
            ParallelSignalCursor<R> c1 = s1.getSignalCursor(true);
            ParallelSignalCursor<R> c2 = s2.getSignalCursor(true);
            double t = Math.max(s1.start(), s2.start());

            spaceItr.init(t, this::addResult);
            DistanceStructure<S, ?> f = spaceItr.generateDistanceStructure();

            //Loop invariant: (current.getFirst() <= time) &&
            //                ((next == null) || (time < next.getFirst()))
            c1.move(t);
            c2.move(t);
            while (isNotCompleted(t, c1, c2)) {
                IntFunction<R> spatialSignal1 = c1.getValue();
                IntFunction<R> spatialSignal2 = c2.getValue();
                List<R> values = reach(domain, spatialSignal1, spatialSignal2, f);
                result.add(t, (values::get));
                double tNext = Math.min(c1.nextTime(), c2.nextTime());
                c1.move(tNext);
                c2.move(tNext);

                while (spaceItr.isNextSpaceModelWithinHorizon(tNext)) {
                    spaceItr.shiftSpatialModel();
                    t = spaceItr.getCurrentT();
                    f = spaceItr.generateDistanceStructure();
                    values = reach(domain, spatialSignal1, spatialSignal2, f);
                    result.add(t, escape(domain, (values::get), f));
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

    @SafeVarargs
    private static <C> boolean isNotCompleted(Double t,
                                              ParallelSignalCursor<C>... cursors)
    {
        return !Double.isNaN(t) &&
                Arrays.stream(cursors)
                      .map(c -> !c.completed())
                      .reduce( true, (c1, c2) -> c1 && c2);
    }
}
