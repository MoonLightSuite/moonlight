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
    List<Update<T, List<R>>> results;

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
        T tNext = u.getEnd();

        tNext = fromNextSpaceOrFallback(tNext);
        return prepareResults(t, tNext, u.getValue());
    }

    private List<Update<T, List<R>>> prepareResults(T t, T tNext, List<R> value)
    {
        final List<List<Update<T, List<R>>>> result = new ArrayList<>();

        shiftSpaceModel(t);

        doCompute(t, tNext, value,
                (a, b, c, d, e) -> result.add(computeOp(a, b, c, d, e)));
        return result.get(0);
    }

    private List<Update<T, List<R>>> computeOp(
            T t, T tNext,
            DistanceStructure<S, ?> f,
            IntFunction<R> spatialSignal,
            Iterator<Pair<T, SpatialModel<S>>> spaceItr)
    {
        results = new ArrayList<>();
        addResult(t, tNext, op.apply(spatialSignal, f));
        moveAndCompute(tNext, spatialSignal, spaceItr);
        return results;
    }

    protected void moveAndCompute(T tNext,
                                  IntFunction<R> spatialSignal,
                                  Iterator<Pair<T, SpatialModel<S>>> spaceItr)
    {
        while (isNextSpaceModelWithinHorizon(tNext))
        {
            currSpace = nextSpace;
            T t = currSpace.getFirst();
            nextSpace = getNext(spaceItr);
            DistanceStructure<S, ?>  f = getDistanceStructure();

            if(isNextSpaceModelMeaningful()) {
                tNext = nextSpace.getFirst();
                addResult(t, tNext, op.apply(spatialSignal, f));
            }
        }
    }

    @Override
    protected void addResult(T start, T end, List<R> value) {
        results.add(new Update<>(start, end, value));
    }

    public TimeChain<T, List<R>> computeUnaryChain(TimeChain<T, List<R>> ups) {
        TimeChain<T, List<R>> rss =  new TimeChain<>(ups.getEnd());
        final int LAST = ups.size() - 1;

        for(int i = 0; i < ups.size(); i++) {
            Sample<T, List<R>> up = ups.get(i);
            T t = up.getStart();
            T tNext = i != LAST ? ups.get(i + 1).getStart() : ups.getEnd();

            doCompute(t, tNext, up.getValue(),
                        (a,b,c,d,e) ->
                            computeOpChain(a, b, c, d, e).forEach(rss::add)
                    );
        }
        return rss;
    }

    private TimeChain<T, List<R>> computeOpChain(
            T t, T tNext,
            DistanceStructure<S, ?> f,
            IntFunction<R> spatialSignal,
            Iterator<Pair<T, SpatialModel<S>>> spaceItr)
    {
        return asTimeChain(computeOp(t, tNext, f, spatialSignal, spaceItr));
    }
}
