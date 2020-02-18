/**
 *
 */
package eu.quanticol.moonlight.signal;

import eu.quanticol.moonlight.formula.DistanceDomain;
import eu.quanticol.moonlight.formula.DoubleDistance;
import eu.quanticol.moonlight.formula.SignalDomain;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.Triple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author loreti
 *
 */
public class DistanceStructure<T, A> {

    private final Function<T, A> distance;

    private final DistanceDomain<A> domain;

    private final A lowerBound;

    private final A upperBound;

    private final SpatialModel<T> model;

    private HashMap<Integer, HashMap<Integer, A>> distanceMatrix;

    public DistanceStructure(Function<T, A> distance, DistanceDomain<A> domain, A lowerBound,
                             A upperBound,
                             SpatialModel<T> model) {
        super();
        this.distance = distance;
        this.domain = domain;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.model = model;
    }

    public A getLowerBound() {
        return lowerBound;
    }

    public A getUpperBound() {
        return upperBound;
    }

    public A getDistance(int i, int j) {
        if (distanceMatrix == null) {
            computeDistanceMatrix();
        }
        HashMap<Integer, A> m = distanceMatrix.get(i);
        if (m != null) {
        	return m.getOrDefault(j, domain.infinity());
        }
        return domain.infinity();
    }

    public A get(int i, int j) {
        if (i == j) {
            return domain.zero();
        } else {
            T e = model.get(i, j);
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
            HashMap<Integer, A> map = new HashMap<>();
            map.put(i, domain.zero());
            distanceMatrix.put(i, map);
        });
        LinkedList<Pair<Integer, Pair<Integer, A>>> queue =
                IntStream
                        .range(0, model.size())
                        .boxed()
                        .map(i -> new Pair<Integer, Pair<Integer, A>>(i, new Pair<Integer, A>(i, domain.zero())))
                        .collect(Collectors.toCollection(LinkedList::new));
        while (!queue.isEmpty()) {
            Pair<Integer, Pair<Integer, A>> p = queue.poll();
            int l1 = p.getFirst();
            int l2 = p.getSecond().getFirst();
            A d1 = p.getSecond().getSecond();
            for (Pair<Integer, T> e : model.previous(l1)) {
                A newD = domain.sum(distance.apply(e.getSecond()), d1);
                HashMap<Integer, A> map = distanceMatrix.get(e.getFirst());
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

    public <R> ArrayList<R> escape(SignalDomain<R> mDomain, Function<Integer, R> s) {
        HashMap<Integer, HashMap<Integer, R>> map = initEscapeMap(mDomain, s);
        ArrayList<HashMap<Integer,R>> pending = IntStream
              .range(0, model.size()).boxed()
              .map((Integer i) -> {
            	  HashMap<Integer,R> m = new HashMap<>();
            	  m.put(i, s.apply(i));
            	  return m;
              }).collect(Collectors.toCollection(ArrayList::new));
        boolean flag = true;
        
        while (flag) {
        	flag = false;
        	ArrayList<HashMap<Integer,R>> newPending = IntStream
        			.range(0, model.size()).boxed().map(i -> new HashMap<Integer,R>())
        			.collect(Collectors.toCollection(ArrayList::new));
        	for( int l1=0 ; l1<model.size() ; l1++ ) {
        		for( Entry<Integer, R> e : pending.get(l1).entrySet()) {
        			int l2 = e.getKey();
        			R v = e.getValue();
                    for (Pair<Integer, T> pre : model.previous(l1)) {
                        int l = pre.getFirst();
                        HashMap<Integer, R> m1 = map.get(l);
                        R v1 = m1.getOrDefault(l2, mDomain.min());
                        R newV = mDomain.disjunction(v1, mDomain.conjunction(s.apply(l), v));
                        if (!mDomain.equalTo(v1, newV)) {
                            m1.put(l2, newV);
                            newPending.get(l).put(l2, newV);
                            flag = true;
                        }
                    }
        		};
        	}
        	pending = newPending;	
        }
        return extractEscapeValues(mDomain, map);
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
//            for (Pair<Integer, T> pre : model.previous(l1)) {
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
    
    public <R> ArrayList<R> reach(SignalDomain<R> mDomain, Function<Integer, R> s1, Function<Integer, R> s2) {
        ArrayList<Map<A, R>> reachFunction =
                IntStream
                        .range(0, model.size()).boxed()
                        .map(i -> new HashMap<A, R>())
                        .collect(Collectors.toCollection(ArrayList::new));
        LinkedList<Triple<Integer, A, R>> queue =
                IntStream
                        .range(0, model.size()).boxed()
                        .map(i -> new Triple<Integer, A, R>(i, domain.zero(), s2.apply(i)))
                        .collect(Collectors.toCollection(LinkedList::new));
        queue.forEach(t -> reachFunction.get(t.getFirst()).put(t.getSecond(), t.getThird()));
        while (!queue.isEmpty()) {
            Triple<Integer, A, R> t1 = queue.poll();
            int l1 = t1.getFirst();
            A d1 = t1.getSecond();
            R v1 = t1.getThird();
            for (Pair<Integer, T> pre : model.previous(l1)) {
                int l2 = pre.getFirst();
                A d2 = domain.sum(distance.apply(pre.getSecond()), d1);
                if (domain.lessOrEqual(d2, upperBound)) {
                    Triple<Integer, A, R> t2 = combine(mDomain, l2, d2, mDomain.conjunction(v1, s1.apply(l2)), reachFunction.get(l2));
                    if (t2 != null) {
                        queue.add(t2);
                    }
                }
            }

        }
        return collectReachValue(mDomain, reachFunction);
    }

    private <R> Triple<Integer, A, R> combine(SignalDomain<R> mDomain, int l, A d, R v,
                                              Map<A, R> fr) {
        R v1 = fr.get(d);
        if (v1 != null) {
            R v2 = mDomain.disjunction(v, v1);
            if (!mDomain.equalTo(v1,v2)) {
                fr.put(d, v2);
                return new Triple<>(l, d, v2);
            }
        } else {
            Triple<Integer, A, R> t = new Triple<>(l, d, v);
            fr.put(d, v);
            return t;
        }
        return null;
    }

    private <R> ArrayList<R> collectReachValue(SignalDomain<R> mDomain, ArrayList<Map<A, R>> reachFunction) {
        return IntStream
                .range(0, model.size()).boxed()
                .map(i -> reachFunction.get(i))
                .map(rf -> computeReachValue(mDomain, rf))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private <R> R computeReachValue(SignalDomain<R> mDomain, Map<A, R> rf) {
        return rf.entrySet()
                .stream()
                .filter(e -> checkDistance(e.getKey()))
                .map(e -> e.getValue())
                .reduce(mDomain.min(), mDomain::disjunction);
    }

    private <R> ArrayList<R> extractEscapeValues(SignalDomain<R> mDomain, HashMap<Integer, HashMap<Integer, R>> map) {
        ArrayList<R> toReturn = mDomain.createArray(model.size());
        for (int i = 0; i < model.size(); i++) {
            R value = mDomain.min();
            HashMap<Integer, R> mI = map.get(i);
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

    private <R> HashMap<Integer, HashMap<Pair<Integer, R>, A>> initReachMap(SignalDomain<R> mDomain,
                                                                            Function<Integer, R> s2) {
        HashMap<Integer, HashMap<Pair<Integer, R>, A>> toReturn = new HashMap<>();
        for (int i = 0; i < model.size(); i++) {
            HashMap<Pair<Integer, R>, A> iR = new HashMap<>();
            iR.put(new Pair<Integer, R>(i, s2.apply(i)), domain.zero());
            toReturn.put(i, iR);
        }
        return toReturn;
    }

    private <R> HashMap<Integer, HashMap<Integer, R>> initEscapeMap(SignalDomain<R> mDomain,
                                                                    Function<Integer, R> s) {
        HashMap<Integer, HashMap<Integer, R>> toReturn = new HashMap<>();
        for (int i = 0; i < model.size(); i++) {
            HashMap<Integer, R> iR = new HashMap<>();
            iR.put(i, s.apply(i));
            toReturn.put(i, iR);
        }
        return toReturn;
    }

//	private <R> HashMap<Integer, HashMap<Integer, Pair<R, A>>> initEscapeMap(SignalDomain<R> mDomain,
//			Function<Integer, R> s2) {
//		HashMap<Integer, HashMap<Integer, Pair<R, A>>> toReturn = new HashMap<>();
//		for( int i=0 ; i<model.size() ; i++ ) {
//			HashMap<Integer, Pair<R, A>> iR = new HashMap<>();
//			iR.put(i,new Pair<R,A>(s2.apply(i), domain.zero()));
//			toReturn.put(i, iR);
//		}
//		return toReturn;
//	}

    public <R> ArrayList<R> everywhere(SignalDomain<R> dModule, Function<Integer, R> s) {
        ArrayList<R> values = dModule.createArray(model.size());
        for (int i = 0; i < model.size(); i++) {
            R v = dModule.max();
            for (int j = 0; j < model.size(); j++) {
                if (this.checkDistance(i, j)) {
                    v = dModule.conjunction(v, s.apply(j));
                }
            }
            values.set(i, v);
        }
        return values;
    }
    
    public <R> ArrayList<R> somewhere(SignalDomain<R> dModule, Function<Integer, R> s) {
        ArrayList<R> values = dModule.createArray(model.size());
        for (int i = 0; i < model.size(); i++) {
            R v = dModule.min();
            for (int j = 0; j < model.size(); j++) {
                if (this.checkDistance(i, j)) {
                    v = dModule.disjunction(v, s.apply(j));
                }
            }
            values.set(i, v);
        }
        return values;
    }

    private <R> ArrayList<ArrayList<R>> createMatrix(int rows, int columns, BiFunction<Integer, Integer, R> init) {
        return IntStream
                .range(0, rows)
                .mapToObj(x -> createArray(columns, y -> init.apply(x, y)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private <R> ArrayList<R> createArray(int size, Function<Integer, R> init) {
        return IntStream
                .range(0, size)
                .mapToObj(x -> init.apply(x))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static <T> DistanceStructure<T, Double> buildDistanceStructure( SpatialModel<T> model , Function<T,Double> distance , double lowerBound, double upperBound) {
    	return new DistanceStructure<T, Double>(distance, new DoubleDistance(), lowerBound, upperBound, model);
    }
}
