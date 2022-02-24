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

import eu.quanticol.moonlight.online.signal.SegmentInterface;
import eu.quanticol.moonlight.online.signal.TimeChain;
import eu.quanticol.moonlight.online.signal.Update;
import eu.quanticol.moonlight.core.space.DefaultDistanceStructure;
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
{
    private final LocationService<T, S> locSvc;
    private final Function<SpatialModel<S>, DefaultDistanceStructure<S, ?>> dist;
    private final BiFunction<IntFunction<R>,
            DefaultDistanceStructure<S, ?>,
                             List<R>> op;

    Pair<T, SpatialModel<S>> currSpace;
    Pair<T, SpatialModel<S>> nextSpace;

    public SpatialComputation(@NotNull LocationService<T, S> locationService,
                              Function<SpatialModel<S>,
                                      DefaultDistanceStructure<S, ?>> distance,
                              BiFunction<IntFunction<R>,
                                      DefaultDistanceStructure<S, ?>,
                                                  List<R>> operator)
    {
        checkLocationServiceValidity(locationService);
        locSvc = locationService;
        dist = distance;
        op = operator;
    }

    private void checkLocationServiceValidity(LocationService<T, S> locSvc)
    {
        if (locSvc.isEmpty())
            throw new UnsupportedOperationException("The location Service " +
                                                    "must not be empty!");
    }

    public List<Update<T, List<R>>> computeUnary(Update<T, List<R>> u)
    {
        Iterator<Pair<T, SpatialModel<S>>> spaceItr = locSvc.times();
        T t = u.getStart();
        T tNext = u.getEnd();
        tNext = seekSpace(t, tNext, spaceItr);

        final List<List<Update<T, List<R>>>> result = new ArrayList<>();
        doCompute(t, tNext, u.getValue(),
                    (a, b, c, d, e) -> result.add(computeOp(a, b, c, d, e)));
        return result.get(0);
    }

    private List<Update<T, List<R>>> computeOp(
            T t, T tNext,
            DefaultDistanceStructure<S, ?> f,
            IntFunction<R> spatialSignal,
            Iterator<Pair<T, SpatialModel<S>>> spaceItr)
    {
        List<Update<T, List<R>>> results = new ArrayList<>();

        results.add(new Update<>(t, tNext, op.apply(spatialSignal, f)));

        while (nextSpace != null &&
                nextSpace.getFirst().compareTo(tNext) < 0)
        {
            currSpace = nextSpace;
            t = currSpace.getFirst();
            nextSpace = getNext(spaceItr);
            if(nextSpace != null && !currSpace.equals(nextSpace)) {
                tNext = nextSpace.getFirst();
                f = dist.apply(currSpace.getSecond());
                results.add(new Update<>(t, tNext, op.apply(spatialSignal, f)));
            }
        }

        return results;
    }

    public TimeChain<T, List<R>> computeUnaryChain(TimeChain<T, List<R>> ups)
    {
        TimeChain<T, List<R>> results =  new TimeChain<>(ups.getEnd());
        final int LAST = ups.size() - 1;

        for(int i = 0; i < ups.size(); i++) {
            SegmentInterface<T, List<R>> up = ups.get(i);
            T t = up.getStart();
            T tNext = i != LAST ? ups.get(i + 1).getStart() : ups.getEnd();

            doCompute(t, tNext, up.getValue(),
                        (a,b,c,d,e) ->
                            computeOpChain(a, b, c, d, e).forEach(results::add)
                    );
        }

        return results;
    }

    private void doCompute(T t, T tNext, List<R> value,
                    FiveParameterFunction<T, T, DefaultDistanceStructure<S, ?>,
                    IntFunction<R>,
                    Iterator<Pair<T, SpatialModel<S>>>> op)
    {
        Iterator<Pair<T, SpatialModel<S>>> spaceItr = locSvc.times();
        IntFunction<R> spatialSignal = value::get;
        tNext = seekSpace(t, tNext, spaceItr);
        SpatialModel<S> sm = currSpace.getSecond();
        DefaultDistanceStructure<S, ?> f = dist.apply(sm);
        f.canReach(0, 0); //TODO: Done to force pre-computation
                                    // of distance matrix

        op.accept(t, tNext, f, spatialSignal, spaceItr);
    }

    @FunctionalInterface
    public interface FiveParameterFunction<T, U, V, W, X> {
        void accept(T t, U u, V v, W w, X x);
    }

    private TimeChain<T, List<R>> computeOpChain(
            T t, T tNext,
            DefaultDistanceStructure<S, ?> f,
            IntFunction<R> spatialSignal,
            Iterator<Pair<T, SpatialModel<S>>> spaceItr)
    {
        return asTimeChain(computeOp(t, tNext, f, spatialSignal, spaceItr));
    }

    private T seekSpace(T start, T end,
                        Iterator<Pair<T, SpatialModel<S>>> spaceItr)
    {
        currSpace = spaceItr.next();
        getNext(spaceItr);
        while(nextSpaceBeforeNextStart(start)) {
            currSpace = nextSpace;
            nextSpace = getNext(spaceItr);
        }
        return fromNextSpaceOrCurrent(end);
    }

    private boolean nextSpaceBeforeNextStart(T start) {
        return nextSpace != null && nextSpace.getFirst().compareTo(start) <= 0;
    }

    private T fromNextSpaceOrCurrent(T fallback) {
        if(nextSpace != null)
            return nextSpace.getFirst();
        return fallback;
    }


    /**
     * Returns the next element if there is one, otherwise null
     * @param itr Location Service Iterator
     * @param <S> Spatial Domain
     * @return Next element of the Location Service
     */
    private static <T, S> Pair<T, SpatialModel<S>> getNext(
            Iterator<Pair<T, SpatialModel<S>>> itr)
    {
        return (itr.hasNext() ? itr.next() : null);
    }
}
