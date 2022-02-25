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
 */

package eu.quanticol.moonlight.core.space;

import eu.quanticol.moonlight.domain.DoubleDistance;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class is a helper class for computing the right distance on dynamic
 * models for Reach and Escape algorithms.
 *
 * @param <E> Type of edge labels of the spatial model.
 * @param <M> Type of the distance metric
 */
public class DefaultDistanceStructure<E, M> implements DistanceStructure<E, M> {
    private final Function<E, M> distanceFunction;
    private final DistanceDomain<M> distanceDomain;
    private final Map<Integer,Map<Integer, M>> distanceMatrix;
    private final M lowerBound;
    private final M upperBound;
    private final SpatialModel<E> model;

    public DefaultDistanceStructure(@NotNull Function<E, M> distanceFunction,
                                    @NotNull DistanceDomain<M> distanceDomain,
                                    @NotNull M lowerBound,
                                    @NotNull M upperBound,
                                    @NotNull SpatialModel<E> model)
    {
        this.distanceFunction = distanceFunction;
        this.distanceDomain = distanceDomain;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.model = model;
        this.distanceMatrix = computeDistanceMatrix();
    }

    @Override
    public SpatialModel<E> getModel() {
        return model;
    }

    @Override
    public Function<E, M> getDistanceFunction() {
        return distanceFunction;
    }

    @Override
    public DistanceDomain<M> getDistanceDomain() {
        return distanceDomain;
    }

    @Override
    public M getDistance(int i, int j) {
        Map<Integer, M> m = distanceMatrix.get(i);
        if (m != null) {
        	return m.getOrDefault(j, distanceDomain.infinity());
        }
        return distanceDomain.infinity();
    }

    @Override
    public boolean areWithinBounds(int from, int to) {
        return isWithinBounds(getDistance(from, to));
    }

    public <R> List<R> escape(SignalDomain<R> signalDomain,
                              IntFunction<R> spatialSignal)
    {

        return new EscapeAlgorithm<>(this, signalDomain, spatialSignal)
                .compute();
    }

    @Override
    public boolean isWithinBounds(M d) {
        return distanceDomain.lessOrEqual(lowerBound, d) &&
                distanceDomain.lessOrEqual(d, upperBound);
    }

    private Map<Integer,Map<Integer, M>> computeDistanceMatrix() {
        Map<Integer, Map<Integer, M>> distanceMap = distanceMapInit();
        Deque<Pair<Integer, Pair<Integer, M>>> queue = distanceQueueInit();
        while(!queue.isEmpty()) {
            Pair<Integer, Pair<Integer, M>> p = queue.poll();
            int l1 = p.getFirst();
            int l2 = p.getSecond().getFirst();
            M d1 = p.getSecond().getSecond();
            computeDistances(distanceMap, queue, l1, l2, d1);
        }
        return distanceMap;
    }

    private void computeDistances(Map<Integer, Map<Integer, M>> distanceMap,
                                  Deque<Pair<Integer, Pair<Integer, M>>> queue,
                                  int l1, int l2, M d1)
    {
        for (Pair<Integer, E> edge: model.previous(l1)) {
            M newD = increaseDistance(d1, edge);
            Map<Integer, M> distances = distanceMap.get(edge.getFirst());
            M oldD = distances.getOrDefault(l2, distanceDomain.infinity());
            if (distanceDomain.less(newD, oldD)) {
                distances.put(l2, newD);
                queue.add(new Pair<>(edge.getFirst(), new Pair<>(l2, newD)));
            }
        }
    }

    private M increaseDistance(M d, Pair<Integer, E> edge) {
        return distanceDomain.sum(distanceFunction.apply(edge.getSecond()), d);
    }

    private LinkedList<Pair<Integer, Pair<Integer, M>>> distanceQueueInit() {
        return IntStream.range(0, model.size()).boxed()
                        .map(i -> new Pair<>(i,
                            new Pair<>(i,
                                distanceDomain.zero())))
                        .collect(Collectors.toCollection(LinkedList::new));
    }

    private Map<Integer, Map<Integer, M>> distanceMapInit() {
        Map<Integer, Map<Integer, M>> distanceMap = new HashMap<>();
        IntStream.range(0, model.size()).forEach(location -> {
            Map<Integer, M> locationDistances = new HashMap<>();
            locationDistances.put(location, distanceDomain.zero());
            distanceMap.put(location, locationDistances);
        });
        return distanceMap;
    }

    /**
     * @deprecated useless and opaque.
     *             Replace with direct constructor call.
     */
    @Deprecated
    public static <T> DefaultDistanceStructure<T, Double>
    buildDistanceStructure(SpatialModel<T> model,
                           Function<T, Double> distance,
                           double lowerBound,
                           double upperBound)
    {
        return new DefaultDistanceStructure<>(distance, new DoubleDistance(),
                lowerBound, upperBound, model);
    }
}
