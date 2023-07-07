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

package io.github.moonlightsuite.moonlight.offline.algorithms;

import io.github.moonlightsuite.moonlight.core.space.DistanceStructure;
import io.github.moonlightsuite.moonlight.core.space.LocationService;
import io.github.moonlightsuite.moonlight.core.space.SpaceIterator;
import io.github.moonlightsuite.moonlight.core.space.SpatialModel;
import io.github.moonlightsuite.moonlight.offline.signal.ParallelSignalCursor1;
import io.github.moonlightsuite.moonlight.offline.signal.Signal;
import io.github.moonlightsuite.moonlight.offline.signal.mfr.MfrSignal;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

/**
 * Algorithm for Reduce operator
 */
public class ReduceOp<S, R, V> {
    private final Function<List<V>, R> aggregator;
    private final LocationService<Double, S> locSvc;
    private final Function<SpatialModel<S>, DistanceStructure<S, ?>> distance;
    private final int size;

    public ReduceOp(int size,
                    LocationService<Double, S> locSvc,
                    Function<SpatialModel<S>, DistanceStructure<S, ?>> distance,
                    Function<List<V>, R> aggregator) {
        this.size = size;
        this.locSvc = locSvc;
        this.distance = distance;
        this.aggregator = aggregator;
    }

    public MfrSignal<R> computeUnary(int[] definitionSet,
                                     IntFunction<MfrSignal<V>> s) {

        if (!locSvc.isEmpty()) {
            return doCompute(s, definitionSet);
        }
        throw new UnsupportedOperationException("Invalid location service " +
                "passed");
    }

    private MfrSignal<R> doCompute(IntFunction<MfrSignal<V>> setSignal,
                                   int[] definitionSet) {
        return new MfrSignal<>(size, i -> reduce(setSignal.apply(i)),
                definitionSet);
    }

    private Signal<R> reduce(MfrSignal<V> arg) {
        var cursor = arg.getSignalCursor(true);
        double t = cursor.getCurrentTime();
        Signal<R> result = new Signal<>();
        var spaceItr = new SpaceIterator<>(locSvc, distance);

        spaceItr.init(t);
        while (!Double.isNaN(t) && isNotCompleted(cursor)) {
            t = aggregateArg(arg, cursor, t, result, spaceItr);
        }

        return result;
    }

    private double aggregateArg(MfrSignal<V> arg,
                                ParallelSignalCursor1<V> cursor,
                                double t,
                                Signal<R> result,
                                SpaceIterator<Double, S> spaceItr) {
        var spatialSignal = cursor.getCurrentValue();
        aggregate(t, arg, result, spatialSignal);
        double tNext = cursor.forwardTime();
        spaceItr.forEach(tNext, (itT, itDs) ->
                aggregate(itT, arg, result, spatialSignal)
        );
        t = moveSpatialModel(tNext, spaceItr);
        return t;
    }

    private void aggregate(Double t, MfrSignal<V> arg,
                           Signal<R> result, IntFunction<V> spatialSignal) {
        var values = locationStream(arg).mapToObj(spatialSignal).toList();
        R aggregated = aggregator.apply(values);
        result.add(t, aggregated);
    }

    private IntStream locationStream(MfrSignal<V> signal) {
        return Arrays.stream(signal.getLocationsSet());
    }

    private Double moveSpatialModel(@NotNull Double t, SpaceIterator<Double, S> spaceItr) {
        if (spaceItr.isNextSpaceModelAtSameTime(t)) {
            spaceItr.shiftSpatialModel();
        }
        return t;
    }

    @SafeVarargs
    private static <C> boolean isNotCompleted(ParallelSignalCursor1<C>... cursors) {
        return Arrays.stream(cursors)
                .map(c -> !c.isCompleted())
                .reduce(true, (c1, c2) -> c1 && c2);
    }
}
