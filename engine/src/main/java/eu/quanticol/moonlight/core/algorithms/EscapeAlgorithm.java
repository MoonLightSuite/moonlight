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

package eu.quanticol.moonlight.core.algorithms;

import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.core.base.Pair;

import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/**
 * Algorithmic class for computing the escape operator
 * @param <E> weight type of the edges of the spatial model
 * @param <M> metric type considered by the distance structure
 * @param <R> signal interpretation type
 */
public class EscapeAlgorithm<E, M, R> {
    private final SpatialModel<E> model;
    private final SignalDomain<R> signalDomain;
    private final IntFunction<R> spatialSignal;
    private final DistanceStructure<E, M> distStr;
    private final Map<Integer, Map<Integer, R>> minimalDistanceMap;

    public EscapeAlgorithm(DistanceStructure<E, M> distStr,
                           SignalDomain<R> signalDomain,
                           IntFunction<R> spatialSignal)
    {
        this.spatialSignal = spatialSignal;
        this.distStr = distStr;
        this.model = distStr.getModel();
        this.signalDomain = signalDomain;
        this.minimalDistanceMap = new HashMap<>();
    }

    /**
     * @return escape's algorithm computation
     */
    public List<R> compute() {
        Set<Pair<Integer, Integer>> neighbourhood = getNeighbourhood();
        while (!neighbourhood.isEmpty()) {
            neighbourhood = updateShortestPaths(neighbourhood);
        }
        return extractResult();
    }

    private Set<Pair<Integer,Integer>> getNeighbourhood() {
        Set<Pair<Integer,Integer>> neighbourhood = new HashSet<>();
        for(int i = 0; i < model.size(); i++) {
            Map<Integer, R> initialDistance = new HashMap<>();
            initialDistance.put(i, spatialSignal.apply(i));
            minimalDistanceMap.put(i, initialDistance);
            neighbourhood.add(new Pair<>(i, i));
        }
        return neighbourhood;
    }

    private Set<Pair<Integer, Integer>> updateShortestPaths(
            Set<Pair<Integer, Integer>> neighbourhood)
    {
        Map<Integer, Map<Integer, R>> neighboursDistanceMap = new HashMap<>();
        Set<Pair<Integer, Integer>> extendedNeighbourhood = new HashSet<>();
        for (Pair<Integer, Integer> pair: neighbourhood) {
            int l1 = pair.getFirst();
            int l2 = pair.getSecond();
            updateDistance(l1, l2, extendedNeighbourhood, neighboursDistanceMap);
        }
        addAll(neighboursDistanceMap);
        return extendedNeighbourhood;
    }

    private void updateDistance(int source, int target,
                            Set<Pair<Integer, Integer>> extendedNeighbourhood,
                            Map<Integer, Map<Integer, R>> neighboursDistanceMap)
    {
        for (int neighbour: getIncomingEdgesLocations(source)) {
            R oldV = getCurrentMinimalDistance(neighbour, target);
            R selfV = getCurrentMinimalDistance(source, source);
            R newV = combine(oldV, spatialSignal.apply(neighbour), selfV);
            if (!signalDomain.equalTo(newV, oldV)) {
                extendedNeighbourhood.add(new Pair<>(neighbour, target));
                addDistancePair(neighboursDistanceMap, neighbour, target, newV);
            }
        }
    }

    private List<Integer> getIncomingEdgesLocations(int location) {
        return model.previous(location).stream()
                    .map(Pair::getFirst)
                    .collect(Collectors.toList());
    }

    private R combine(R oldV, R signalV, R selfV) {
        return signalDomain.disjunction(oldV,
                signalDomain.conjunction(signalV, selfV));
    }

    private void addAll(Map<Integer, Map<Integer, R>> eMapNext)
    {
        eMapNext.forEach((l1, distanceMap) ->
                distanceMap.forEach((l2, distance) ->
                        addDistancePair(minimalDistanceMap, l1, l2, distance)));
    }

    private R getCurrentMinimalDistance(int l1, int l2) {
        return minimalDistanceMap.get(l1).getOrDefault(l2, signalDomain.min());
    }

    private void addDistancePair(Map<Integer, Map<Integer, R>> map,
                                 int l1, int l2, R v)
    {
        Map<Integer,R> m = map.computeIfAbsent(l1, x -> new HashMap<>());
        m.put(l2, v);
    }

    private List<R> extractResult() {
        List<R> toReturn = signalDomain.createArray(model.size());
        for (int i = 0; i < model.size(); i++) {
            Map<Integer, R> minimalDistance = minimalDistanceMap.get(i);
            Set<Map.Entry<Integer, R>> entries = minimalDistance.entrySet();
            R value = updateValue(entries, i);
            toReturn.set(i, value);
        }
        return toReturn;
    }

    private R updateValue(Set<Map.Entry<Integer, R>> entries, int i) {
        R value = signalDomain.min();
        for (Map.Entry<Integer, R> k: entries) {
            int location = k.getKey();
            R distance = k.getValue();
            if (distStr.areWithinBounds(i, location)) {
                value = signalDomain.disjunction(value, distance);
            }
        }
        return value;
    }
}
