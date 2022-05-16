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

import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpaceIterator;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.offline.signal.ParallelSignalCursor;
import eu.quanticol.moonlight.offline.signal.Signal;
import eu.quanticol.moonlight.offline.signal.mfr.MfrSignal;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

import static eu.quanticol.moonlight.offline.algorithms.mfr.MfrAlgorithm.getAllWithinDistance;

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

    public MfrSignal<R> computeUnary(int[] locationsSet,
                                     IntFunction<MfrSignal<V>> s) {

        if (!locSvc.isEmpty()) {
            return doCompute(s, locationsSet);
        }
        throw new UnsupportedOperationException("Invalid location service " +
                "passed");
    }

    private MfrSignal<R> doCompute(IntFunction<MfrSignal<V>> setSignal,
                                   int[] locations) {
        return new MfrSignal<>(size, i -> reduce(i, setSignal.apply(i)),
                locations);
    }

    private Signal<R> reduce(int i, MfrSignal<V> arg) {
        ParallelSignalCursor<V> cursor = arg.getSignalCursor(true);
        double t = cursor.getCurrentTime();
        Signal<R> result = new Signal<>();
        var spaceItr = new SpaceIterator<>(locSvc, distance);
        spaceItr.init(t);
        while (!Double.isNaN(t) && isNotCompleted(cursor)) {
            DistanceStructure<S, ?> ds = spaceItr.generateDistanceStructure();
            var locationSet = getAllWithinDistance(i, size, ds);
            IntFunction<V> spatialSignal = cursor.getCurrentValue();
            List<V> values =
                    Arrays.stream(locationSet).mapToObj(spatialSignal).toList();
            R aggregated = aggregator.apply(values);
            result.add(t, aggregated);
            double tNext = cursor.forwardTime();
            spaceItr.forEach(tNext, (itT, itDs) -> {
                List<V> itValues =
                        Arrays.stream(locationSet).mapToObj(spatialSignal).toList();
                R itAggregated = aggregator.apply(itValues);
                result.add(itT, itAggregated);
            });
            t = moveSpatialModel(tNext, spaceItr);
        }
        return result;
    }

    @SafeVarargs
    private static <C> boolean isNotCompleted(ParallelSignalCursor<C>... cursors) {
        return Arrays.stream(cursors)
                .map(c -> !c.isCompleted())
                .reduce(true, (c1, c2) -> c1 && c2);
    }

    private Double moveSpatialModel(@NotNull Double t, SpaceIterator<Double, S> spaceItr) {
        if (spaceItr.isNextSpaceModelAtSameTime(t)) {
            spaceItr.shiftSpatialModel();
        }
        return t;
    }
}
