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
 * @author loreti
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

    public <R> List<R> reach(SignalDomain<R> signalDomain,
                             IntFunction<R> leftSpatialSignal,
                             IntFunction<R> rightSpatialSignal)
    {
        return new ReachAlgorithm<>(this,
                                    signalDomain,
                                    leftSpatialSignal,
                                    rightSpatialSignal)
                .compute();
    }

    @Override
    public boolean isWithinBounds(M d) {
        return distanceDomain.lessOrEqual(lowerBound, d) &&
                distanceDomain.lessOrEqual(d, upperBound);
    }

    private Map<Integer,Map<Integer, M>> computeDistanceMatrix() {
        Map<Integer, Map<Integer, M>> distM = new HashMap<>();
        IntStream.range(0, model.size()).forEach(i -> {
            Map<Integer, M> map = new HashMap<>();
            map.put(i, distanceDomain.zero());
            distM.put(i, map);
        });
        LinkedList<Pair<Integer, Pair<Integer, M>>> queue =
                IntStream
                        .range(0, model.size())
                        .boxed()
                        .map(i -> new Pair<>(i, new Pair<>(i, distanceDomain.zero())))
                        .collect(Collectors.toCollection(LinkedList::new));
        while (!queue.isEmpty()) {
            Pair<Integer, Pair<Integer, M>> p = queue.poll();
            int l1 = p.getFirst();
            int l2 = p.getSecond().getFirst();
            M d1 = p.getSecond().getSecond();
            for (Pair<Integer, E> e : model.previous(l1)) {
                M newD = distanceDomain.sum(distanceFunction.apply(e.getSecond()), d1);
                Map<Integer, M> map = distM.get(e.getFirst());
                M oldD = map.getOrDefault(l2, distanceDomain.infinity());
                if (distanceDomain.less(newD, oldD)) {
                    map.put(l2, newD);
                    queue.add(new Pair<>(e.getFirst(), new Pair<>(l2, newD)));
                }
            }
        }
        return distM;
    }

    /**
     * @deprecated replace with constructor
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
