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

import eu.quanticol.moonlight.core.space.DistanceDomain;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.core.base.Pair;
import eu.quanticol.moonlight.core.base.Triple;

import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/**
 * Algorithmic class for computing the reach operator
 * @param <E> weight type of the edges of the spatial model
 * @param <M> metric type considered by the distance structure
 * @param <R> signal interpretation type
 */
public class ReachAlgorithm<E, M, R> {
    private final SpatialModel<E> model;
    private final SignalDomain<R> signalDomain;
    private final List<R> leftSpatialSignal;
    private final List<R> rightSpatialSignal;
    private final DistanceStructure<E, M> distStr;
    private final DistanceDomain<M> distanceDomain;
    private final List<Triple<Integer, M, R>> reachabilityQueue;

    /**
     * This list is quite special: the i-th element of the list denotes
     * the set of optimal values for each distance from location i.
     */
    private final List<Map<M, R>> reachabilityFunction;

    public ReachAlgorithm(DistanceStructure<E, M> distStr,
                          SignalDomain<R> signalDomain,
                          List<R> leftSpatialSignal,
                          List<R> rightSpatialSignal) {
        this.leftSpatialSignal = leftSpatialSignal;
        this.rightSpatialSignal = rightSpatialSignal;
        this.distStr = distStr;
        this.model = distStr.getModel();
        this.distanceDomain = distStr.getDistanceDomain();
        this.signalDomain = signalDomain;
        this.reachabilityFunction = new ArrayList<>();
        this.reachabilityQueue = new LinkedList<>();
    }

    /**
     * @return reach's algorithm computation
     */
    public List<R> compute() {
        initReachableValues();
        reachCore();
        return selectMaxReachabilityValues();
    }

    private void initReachableValues() {
        for(int loc = 0; loc < model.size(); loc++) {
            Map<M, R> reachableValues = fetchInitialReachableValues(loc);
            reachabilityFunction.add(reachableValues);
        }
    }

    private Map<M, R> fetchInitialReachableValues(int location)
    {
        Map<M, R> reachableValues = new HashMap<>();
        R rightValue = rightSpatialSignal.get(location);
        updateReachableValue(location,
                             distanceDomain.zero(),
                             rightValue,
                             reachableValues);
        return reachableValues;
    }

    private void reachCore() {
        while (!reachabilityQueue.isEmpty()) {
            Triple<Integer, M, R> t1 = reachabilityQueue.remove(0);
            int l1 = t1.getFirst();
            M d1 = t1.getSecond();
            R v1 = t1.getThird();
            updateReachability(l1, d1, v1);
        }
    }

    private void updateReachability(int l1, M d1, R v1) {
        for (Pair<Integer, E> neighbour: model.previous(l1)) {
            int l2 = neighbour.getFirst();
            M d2 = newDistance(d1, neighbour.getSecond());
            // Note: old condition was distanceDomain.lessOrEqual(d2, upperBound)
            // but I think this one is more correct
            if (distStr.isWithinBounds(d2)) {
                R value = signalDomain.conjunction(v1,
                                                   leftSpatialSignal.get(l2));
                evaluateReachabilityUpdate(l2, d2, value);
            }
        }
    }

    private M newDistance(M d1, E weight) {
        return distanceDomain.sum(
                distStr.getDistanceFunction().apply(weight),
                d1);
    }

    private void evaluateReachabilityUpdate(int location, M distance, R value) {
        Map<M, R> reachableValues = reachabilityFunction.get(location);
        if(reachableValues.containsKey(distance)) {
            R oldValue = reachableValues.get(distance);
            value = signalDomain.disjunction(value, oldValue);
            if (!signalDomain.equalTo(oldValue, value)) {
                updateReachableValue(location, distance, value, reachableValues);
            }
        } else {
            updateReachableValue(location, distance, value, reachableValues);
        }
    }

    private void updateReachableValue(int location,
                                      M distance,
                                      R newValue,
                                      Map<M, R> values)
    {
        values.put(distance, newValue);
        reachabilityQueue.add(new Triple<>(location, distance, newValue));
    }

    private List<R> selectMaxReachabilityValues() {
        return reachabilityFunction.stream()
                        .map(this::maximizeLocationValue)
                        .collect(Collectors.toCollection(ArrayList::new));
    }

    private R maximizeLocationValue(Map<M, R> reachabilityMap) {
        return reachabilityMap.entrySet().stream()
                .filter(e -> distStr.isWithinBounds(e.getKey()))
                .map(Map.Entry::getValue)
                .reduce(signalDomain.min(), signalDomain::disjunction);
    }
}
