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

package eu.quanticol.moonlight.algorithms.online;

import eu.quanticol.moonlight.signal.online.Update;
import eu.quanticol.moonlight.space.DistanceStructure;
import eu.quanticol.moonlight.space.LocationService;
import eu.quanticol.moonlight.space.SpatialModel;
import eu.quanticol.moonlight.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SpatialComputation
<T extends Comparable<T> & Serializable, S, R extends Comparable<R>>
{
    private final LocationService<T, S> locSvc;
    private final Function<SpatialModel<S>, DistanceStructure<S, ?>> dist;
    private final BiFunction<Function<Integer, R>,
                             DistanceStructure<S, ?>,
                             List<R>> op;

    Iterator<Pair<T, SpatialModel<S>>> spaceItr;
    Pair<T, SpatialModel<S>> currSpace;
    Pair<T, SpatialModel<S>> nextSpace;

    public SpatialComputation(LocationService<T, S> locationService,
                              Function<SpatialModel<S>,
                                       DistanceStructure<S, ?>> distance,
                              BiFunction<Function<Integer, R>,
                                                  DistanceStructure<S, ?>,
                                                  List<R>> operator)
    {
        if (locationService.isEmpty())
            throw new UnsupportedOperationException("The location Service " +
                    "must not be empty!");

        locSvc = locationService;
        dist = distance;
        op = operator;
    }

    public List<Update<T, List<R>>> computeDynamic(Update<T, List<R>> u)
    {
        List<Update<T, List<R>>> results = new ArrayList<>();

        spaceItr = locSvc.times();

        T t = u.getStart();
        T tNext = u.getEnd();
        Function<Integer, R> spatialSignal = i -> u.getValue().get(i);

        tNext = seekSpace(t, tNext);

        SpatialModel<S> sm = currSpace.getSecond();
        DistanceStructure<S, ?> f = dist.apply(sm);

        results.add(new Update<>(t, tNext, op.apply(spatialSignal, f)));

        while (nextSpace != null &&
               nextSpace.getFirst().compareTo(u.getEnd()) < 0)
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



    private T seekSpace(T start, T end) {
        T value = end;
        currSpace = spaceItr.next();
        getNext(spaceItr);
        while(nextSpace != null && nextSpace.getFirst().compareTo(start) <= 0) {
            currSpace = nextSpace;
            nextSpace = getNext(spaceItr);
        }

        if(nextSpace != null)
            value = nextSpace.getFirst();

        return value;
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
