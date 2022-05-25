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

import eu.quanticol.moonlight.core.signal.Sample;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpaceIterator;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.online.signal.TimeChain;
import eu.quanticol.moonlight.online.signal.Update;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static eu.quanticol.moonlight.online.signal.Update.asTimeChain;

public class SpatialOp
        <T extends Comparable<T>, S, R extends Comparable<R>> {
    private final SpaceIterator<T, S> spaceIterator;
    private final BiFunction<IntFunction<R>, DistanceStructure<S, ?>, IntFunction<R>> op;
    private List<Update<T, List<R>>> results;
    private int locations;

    public SpatialOp(@NotNull LocationService<T, S> locationService,
                     Function<SpatialModel<S>,
                             DistanceStructure<S, ?>> distance,
                     BiFunction<IntFunction<R>,
                             DistanceStructure<S, ?>,
                             IntFunction<R>> operator) {
        this.op = operator;
        checkLocationServiceValidity(locationService);
        spaceIterator = new SpaceIterator<>(locationService, distance);
    }

    private void checkLocationServiceValidity(LocationService<T, S> locSvc) {
        if (locSvc.isEmpty())
            throw new UnsupportedOperationException("The location Service " +
                    "must not be empty!");
    }

    public List<Update<T, List<R>>> computeUnary(Update<T, List<R>> u) {
        locations = u.getValue().size();
        T t = u.getStart();
        spaceIterator.init(t);
        T tNext = spaceIterator.fromNextSpaceOrFallback(u.getEnd());
        doCompute(t, tNext, u.getValue());
        return results;
    }

    protected void doCompute(T t, T tNext, List<R> value) {
        DistanceStructure<S, ?> f = spaceIterator.generateDistanceStructure();

        tNext = spaceIterator.fromNextSpaceOrFallback(tNext);
        results = new ArrayList<>();
        addResult(t, tNext, op.apply(value::get, f));
        T finalTNext = tNext;
        spaceIterator.forEach(tNext, (itT, itDs) ->
                addResult(itT, finalTNext, op.apply(value::get, itDs))
        );
    }

    protected void addResult(T start, T end, IntFunction<R> value) {
        results.add(new Update<>(start, end, computeSS(value, locations)));
    }

    private List<R> computeSS(IntFunction<R> spatialSignal, int size) {
        return IntStream.range(0, size)
                .boxed()
                .map(spatialSignal::apply)
                .toList();
    }

    public TimeChain<T, List<R>> computeUnaryChain(TimeChain<T, List<R>> ups) {
        TimeChain<T, List<R>> resultsChain = new TimeChain<>(ups.getEnd());
        final int LAST = ups.size() - 1;
        locations = ups.getFirst().getValue().size();

        spaceIterator.init(ups.getStart());

        for (int i = 0; i < ups.size(); i++) {
            Sample<T, List<R>> up = ups.get(i);
            T t = up.getStart();
            T tNext = i != LAST ? ups.get(i + 1).getStart() : ups.getEnd();

            doCompute(t, tNext, up.getValue());
            asTimeChain(results).forEach(resultsChain::add);
        }
        return resultsChain;
    }
}
