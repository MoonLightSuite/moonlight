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

import eu.quanticol.moonlight.core.algorithms.SpatialOperator;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.signal.Sample;
import eu.quanticol.moonlight.online.signal.TimeChain;
import eu.quanticol.moonlight.online.signal.Update;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.util.Pair;
import org.jetbrains.annotations.NotNull;

import static eu.quanticol.moonlight.online.signal.Update.asTimeChain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

public class SpatialComputation
<T extends Comparable<T> & Serializable, S, R extends Comparable<R>>
    extends SpatialOperator<T, S, R>
{
    private List<Update<T, List<R>>> results;
    TimeChain<T, List<R>> resultsChain;

    public SpatialComputation(@NotNull LocationService<T, S> locationService,
                              Function<SpatialModel<S>,
                                      DistanceStructure<S, ?>> distance,
                              BiFunction<IntFunction<R>,
                                      DistanceStructure<S, ?>,
                                      List<R>> operator) {
        super(locationService, distance, operator);
        checkLocationServiceValidity(locationService);
    }

    private void checkLocationServiceValidity(LocationService<T, S> locSvc)
    {
        if (locSvc.isEmpty())
            throw new UnsupportedOperationException("The location Service " +
                                                    "must not be empty!");
    }

    public List<Update<T, List<R>>> computeUnary(Update<T, List<R>> u) {
        T t = u.getStart();
        T tNext = fromNextSpaceOrFallback(u.getEnd());
        toFirstSpatialModel(t);
        doCompute(t, tNext, u.getValue());
        return results;
    }

    protected void doCompute(T t, T tNext, List<R> value) {
        Iterator<Pair<T, SpatialModel<S>>> spaceItr = toFirstSpatialModel(t);
        DistanceStructure<S, ?> f = getDistanceStructure();

        IntFunction<R> spatialSignal = value::get;
        tNext = fromNextSpaceOrFallback(tNext);
        this.results = new ArrayList<>();
        computeOp(t, tNext, f, spatialSignal, spaceItr);
    }

    @Override
    protected void addResult(T start, T end, List<R> value) {
        results.add(new Update<>(start, end, value));
    }

    public TimeChain<T, List<R>> computeUnaryChain(TimeChain<T, List<R>> ups) {
        resultsChain =  new TimeChain<>(ups.getEnd());
        final int LAST = ups.size() - 1;

        for(int i = 0; i < ups.size(); i++) {
            Sample<T, List<R>> up = ups.get(i);
            T t = up.getStart();
            T tNext = i != LAST ? ups.get(i + 1).getStart() : ups.getEnd();

            doCompute(t, tNext, up.getValue());
            asTimeChain(results).forEach(resultsChain::add);
        }
        return resultsChain;
    }
}
