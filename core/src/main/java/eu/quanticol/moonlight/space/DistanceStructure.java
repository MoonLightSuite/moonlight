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

package eu.quanticol.moonlight.space;

import eu.quanticol.moonlight.domain.DistanceDomain;
import eu.quanticol.moonlight.domain.DoubleDistance;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.Triple;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static eu.quanticol.moonlight.algorithms.SpaceUtilities.*;

/**
 * This class is a helper class for computing the right distance on dynamic
 * models for Reach and Escape algorithms.
 *
 * @param <E> Type of edge labels of the spatial model.
 * @param <A>
 * @author loreti
 */
public class DistanceStructure<E, A> {
    private final DistanceDomain<A> domain;

    /**
     * Lower and upper bound of the spatial operator being computed
     * e.g. <code>escape_[a, b] P</code> means
     * <code>lowerBound == a</code> and <code>upperBound == b</code>
     */
    private final A lowerBound;
    private final A upperBound;

    private final SpatialModel<E> model;

    private final Function<E, A> distance;
    private Map<Integer, Map<Integer, A>> distanceMatrix;

    public DistanceStructure(Function<E, A> distance,
                             DistanceDomain<A> domain,
                             A lowerBound,
                             A upperBound,
                             SpatialModel<E> model)
    {
        super();
        this.distance = distance;
        this.domain = domain;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.model = model;
    }

    public int getModelSize() {
        if(model != null)
            return model.size();

        throw new UnsupportedOperationException("Requesting size of empty model");
    }

    //TODO: a getter with side effects...refactor asap
    public A getDistance(int i, int j) {
        if (distanceMatrix == null) {
            computeDistanceMatrix();
        }
        Map<Integer, A> m = distanceMatrix.get(i);
        if (m != null) {
        	return m.getOrDefault(j, domain.infinity());
        }
        return domain.infinity();
    }

    public A get(int i, int j) {
        if (i == j) {
            return domain.zero();
        } else {
            E e = model.get(i, j);
            if (e == null) {
                return domain.infinity();
            } else {
                return distance.apply(e);
            }
        }
    }

    private void computeDistanceMatrix() {
        distanceMatrix = new HashMap<>();
        IntStream.range(0, model.size()).forEach(i -> {
            Map<Integer, A> map = new HashMap<>();
            map.put(i, domain.zero());
            distanceMatrix.put(i, map);
        });
        LinkedList<Pair<Integer, Pair<Integer, A>>> queue =
                IntStream
                        .range(0, model.size())
                        .boxed()
                        .map(i -> new Pair<>(i, new Pair<>(i, domain.zero())))
                        .collect(Collectors.toCollection(LinkedList::new));
        while (!queue.isEmpty()) {
            Pair<Integer, Pair<Integer, A>> p = queue.poll();
            int l1 = p.getFirst();
            int l2 = p.getSecond().getFirst();
            A d1 = p.getSecond().getSecond();
            for (Pair<Integer, E> e : model.previous(l1)) {
                A newD = domain.sum(distance.apply(e.getSecond()), d1);
                Map<Integer, A> map = distanceMatrix.get(e.getFirst());
                A oldD = map.getOrDefault(l2, domain.infinity());
                if (domain.less(newD, oldD)) {
                    map.put(l2, newD);
                    queue.add(new Pair<>(e.getFirst(), new Pair<>(l2, newD)));
                }
            }
        }
    }

    public boolean checkDistance(int i, int j) {
        return checkDistance(getDistance(i, j));
    }

    public <R> List<R> escape(SignalDomain<R> mDomain, IntFunction<R> s) {
        int size = model.size();
        Map<Integer, Map<Integer, R>> map = initEscapeMap(s, size);
        List<Map<Integer,R>> pending = IntStream
              .range(0, size).boxed()
              .map((Integer i) -> {
            	  Map<Integer,R> m = new HashMap<>();
            	  m.put(i, s.apply(i));
            	  return m;
              }).collect(Collectors.toCollection(ArrayList::new));

        boolean flag = true;
        while (flag) {
        	flag = false;
        	List<Map<Integer,R>> newPending = IntStream
        			.range(0, model.size()).boxed().map(i -> new HashMap<Integer,R>())
        			.collect(Collectors.toCollection(ArrayList::new));
        	for(int l1 = 0; l1 < model.size(); l1++) {
        		for(Entry<Integer, R> e : pending.get(l1).entrySet()) {
        			int l2 = e.getKey();
        			R v = e.getValue();
                    for (Pair<Integer, E> pre : model.previous(l1)) {
                        int l = pre.getFirst();
                        Map<Integer, R> m1 = map.get(l);
                        R v1 = m1.getOrDefault(l2, mDomain.min());
                        R newV = mDomain.disjunction(v1, mDomain.conjunction(s.apply(l), v));
                        if (!mDomain.equalTo(v1, newV)) {
                            m1.put(l2, newV);
                            newPending.get(l).put(l2, newV);
                            flag = true;
                        }
                    }
        		}
        	}
        	pending = newPending;	
        }
        return extractEscapeValues(mDomain, map);
    }

    public <R> Map<Integer, Map<Integer, R>> initEscapeMap(IntFunction<R> s,
                                                           int size)
    {
        Map<Integer, Map<Integer, R>> toReturn = new HashMap<>();
        for (int i = 0; i < size; i++) {
            Map<Integer, R> iR = new HashMap<>();
            iR.put(i, s.apply(i));
            toReturn.put(i, iR);
        }
        return toReturn;
    }


    public <R> List<R> reach(SignalDomain<R> mDomain,
                             IntFunction<R> s1,
                             IntFunction<R> s2)
    {
        List<Map<A, R>> reachFunc = new ArrayList<>();
        List<Triple<Integer, A, R>> queue = new LinkedList<>();

        initReach(s2, reachFunc, queue);

        reachCore(queue, reachFunc, mDomain, s1);

        return collectReachValue(mDomain, reachFunc);
    }

    private <R> void initReach(IntFunction<R> s2,
                               List<Map<A, R>> reachFunc,
                               List<Triple<Integer, A, R>> queue)
    {
        for(int i = 0; i < model.size(); i++) {
            Map<A, R> map = new HashMap<>();
            queue.add(new Triple<>(i, domain.zero(), s2.apply(i)));
            map.put(domain.zero(), s2.apply(i));
            reachFunc.add(map);
        }
    }

    private <R> void reachCore(List<Triple<Integer, A, R>> queue,
                              List<Map<A, R>> reachFunc,
                              SignalDomain<R> mDomain,
                              IntFunction<R> s1)
    {
        while (!queue.isEmpty()) {
            Triple<Integer, A, R> t1 = queue.remove(0);
            int l1 = t1.getFirst();
            A d1 = t1.getSecond();
            R v1 = t1.getThird();
            for (Pair<Integer, E> pre : model.previous(l1)) {
                int l2 = pre.getFirst();
                A d2 = domain.sum(distance.apply(pre.getSecond()), d1);
                if (domain.lessOrEqual(d2, upperBound)) {
                    Triple<Integer, A, R> t2 = combine(mDomain, l2, d2,
                            mDomain.conjunction(v1, s1.apply(l2)), reachFunc.get(l2));
                    if (t2 != null) {
                        queue.add(t2);
                    }
                }
            }
        }
    }

    private <R> List<R> collectReachValue(SignalDomain<R> mDomain,
                                          List<Map<A, R>> reachFunction)
    {
        return reachFunction.stream()
                            .map(rf -> computeReachValue(mDomain, rf))
                            .collect(Collectors.toCollection(ArrayList::new));
    }

    private <R> R computeReachValue(SignalDomain<R> mDomain, Map<A, R> rf)
    {
        return rf.entrySet()
                .stream()
                .filter(e -> checkDistance(e.getKey()))
                .map(Entry::getValue)
                .reduce(mDomain.min(), mDomain::disjunction);
    }

    private <R> ArrayList<R> extractEscapeValues(
            SignalDomain<R> mDomain,
            Map<Integer, Map<Integer, R>> map)
    {
        ArrayList<R> toReturn = mDomain.createArray(model.size());
        for (int i = 0; i < model.size(); i++) {
            R value = mDomain.min();
            Map<Integer, R> mI = map.get(i);
            for (Entry<Integer, R> k : mI.entrySet()) {
                if (checkDistance(getDistance(i, k.getKey()))) {
                    value = mDomain.disjunction(value, k.getValue());
                }
            }
            toReturn.set(i, value);
        }
        return toReturn;
    }

    private boolean checkDistance(A d) {
        return domain.lessOrEqual(lowerBound, d) && domain.lessOrEqual(d, upperBound);
    }

    public static <T> DistanceStructure<T, Double> buildDistanceStructure(
            SpatialModel<T> model,
            Function<T, Double> distance,
            double lowerBound,
            double upperBound)
    {
        return new DistanceStructure<>(distance, new DoubleDistance(),
                lowerBound, upperBound, model);
    }

    // UNUSED STUFF...

    private <R> Map<Integer, Map<Pair<Integer, R>, A>> initReachMap(
            IntFunction<R> s2)
    {
        Map<Integer, Map<Pair<Integer, R>, A>> toReturn = new HashMap<>();
        for (int i = 0; i < model.size(); i++) {
            Map<Pair<Integer, R>, A> iR = new HashMap<>();
            iR.put(new Pair<>(i, s2.apply(i)), domain.zero());
            toReturn.put(i, iR);
        }
        return toReturn;
    }

    private <R> ArrayList<ArrayList<R>> createMatrix(
            int rows,
            int columns,
            BiFunction<Integer, Integer, R> init)
    {
        return IntStream
                .range(0, rows)
                .mapToObj(x -> createArray(columns, y -> init.apply(x, y)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private <R> ArrayList<R> createArray(int size, IntFunction<R> init) {
        return IntStream
                .range(0, size)
                .mapToObj(init)
                .collect(Collectors.toCollection(ArrayList::new));
    }



//    public <R> ArrayList<R> escape(SignalDomain<R> mDomain, Function<Integer, R> s) {
//        HashMap<Integer, HashMap<Integer, R>> map = initEscapeMap(mDomain, s);
//        LinkedList<Pair<Integer, Pair<Integer, R>>> queue =
//                IntStream
//                        .range(0, model.size()).boxed()
//                        .map(i -> new Pair<>(i, new Pair<>(i, s.apply(i))))
//                        .collect(Collectors.toCollection(LinkedList::new));
//        while (!queue.isEmpty()) {
//            System.out.println(queue.size());
//            Pair<Integer, Pair<Integer, R>> p = queue.poll();
//            int l1 = p.getFirst();
//            int l2 = p.getSecond().getFirst();
//            R v = p.getSecond().getSecond();
//            for (Pair<Integer, E> pre : model.previous(l1)) {
//                int l = pre.getFirst();
//                HashMap<Integer, R> m1 = map.get(l);
//                R v1 = m1.getOrDefault(l2, mDomain.min());
//                R newV = mDomain.disjunction(v1, mDomain.conjunction(s.apply(l), v));
//                if (!mDomain.equalTo(v1, newV)) {
//                    m1.put(l2, newV);
//                    queue.add(new Pair<>(l, new Pair<>(l2, newV)));
//                }
//            }
//
//        }
//        return extractEscapeValues(mDomain, map);
//    }
// 	private <R> HashMap<Integer, HashMap<Integer, Pair<R, A>>> initEscapeMap(SignalDomain<R> mDomain,
//			Function<Integer, R> s2) {
//		HashMap<Integer, HashMap<Integer, Pair<R, A>>> toReturn = new HashMap<>();
//		for( int i=0 ; i<model.size() ; i++ ) {
//			HashMap<Integer, Pair<R, A>> iR = new HashMap<>();
//			iR.put(i,new Pair<R,A>(s2.apply(i), domain.zero()));
//			toReturn.put(i, iR);
//		}
//		return toReturn;
//	}
}
