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

package eu.quanticol.moonlight.offline.algorithms.mfr;

import eu.quanticol.moonlight.core.algorithms.SpaceIterator;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.offline.signal.ParallelSignalCursor;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * Algorithm for Somewhere and Everywhere Computation
 */
public class MfrOp<S, T, R> {
    private final SpaceIterator<Double, S, R> spaceItr;
    ParallelSignalCursor<T> cursor;
    private SpatialTemporalSignal<R> result;

    public MfrOp(
            LocationService<Double, S> l,
            Function<SpatialModel<S>, DistanceStructure<S, ?>> distance,
            BiFunction<IntFunction<R>, DistanceStructure<S, ?>,
                    IntFunction<R>> operator) {
        spaceItr = new SpaceIterator<>(l, distance, operator);
    }

    @SafeVarargs
    private static <C> boolean isNotCompleted(ParallelSignalCursor<C>... cursors) {
        return
                Arrays.stream(cursors)
                        .map(c -> !c.isCompleted())
                        .reduce(true, (c1, c2) -> c1 && c2);
    }

    public SpatialTemporalSignal<R> computeUnary(SpatialTemporalSignal<T> s) {
        outputInit(s.getNumberOfLocations());
        if (!spaceItr.isLocationServiceEmpty()) {
            doCompute(s);
        }
        return result;
    }

    private void outputInit(int locations) {
        result = new SpatialTemporalSignal<>(locations);
    }

    private void doCompute(SpatialTemporalSignal<T> s) {
        cursor = s.getSignalCursor(true);
        double t = cursor.getCurrentTime();
        spaceItr.init(t, this::addResult);
        DistanceStructure<S, ?> ds = spaceItr.generateDistanceStructure();

        while (!Double.isNaN(t) && isNotCompleted(cursor)) {
            IntFunction<T> spatialSignal = cursor.getCurrentValue();
            double tNext = cursor.forwardTime();
            spaceItr.computeOp(t, tNext, ds, null);
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
}
