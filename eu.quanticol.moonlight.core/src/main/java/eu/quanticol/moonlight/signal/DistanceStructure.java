/**
 * 
 */
package eu.quanticol.moonlight.signal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import eu.quanticol.moonlight.formula.DistanceDomain;
import eu.quanticol.moonlight.formula.SignalDomain;
import eu.quanticol.moonlight.util.Pair;

/**
 * @author loreti
 *
 */
public class DistanceStructure<T,A> {
	
	private final Function<T,A> distance;
	
	private final DistanceDomain<A> domain;
	
	private final Predicate<A> bound;
	
	private final SpatialModel<T> model;
	
	private HashMap<Integer,HashMap<Integer,A>> distanceMatrix;
	
	/**
	 * @param distance
	 * @param domain
	 * @param guard
	 * @param model
	 */
	public DistanceStructure(Function<T, A> distance, DistanceDomain<A> domain, Predicate<A> bound,
			SpatialModel<T> model) {
		super();
		this.distance = distance;
		this.domain = domain;
		this.bound = bound;
		this.model = model;
	}
	
	public Predicate<A> getBound( ) {
		return bound;
	}
	
	public A getDistance( int i , int j ) {
		if (distanceMatrix==null) {
			computeDistanceMatrix();
		}
		return distanceMatrix.get(i).get(j);
	}

	public A get( int i , int j ) {
		if (i==j) {
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
			HashMap<Integer,A> map = new HashMap<>();
			map.put(i, domain.zero());
			distanceMatrix.put(i, map);			
		});
		LinkedList<Pair<Integer,Pair<Integer, A>>> queue = 
				IntStream
					.range(0, model.size())
					.boxed()
					.map(i -> new Pair<Integer,Pair<Integer,A>>(i,new Pair<Integer,A>(i,domain.zero())))
					.collect(Collectors.toCollection(LinkedList::new));
		while (!queue.isEmpty()) {
			Pair<Integer,Pair<Integer, A>> p = queue.poll();
			int l1 = p.getFirst();
			int l2 = p.getSecond().getFirst();
			A d1 = p.getSecond().getSecond();
			for (Pair<Integer, T> e: model.previous(l1)) {
				A newD = domain.sum(distance.apply(e.getSecond()), d1);
				HashMap<Integer,A> map = distanceMatrix.get(e.getFirst());
				A oldD = map.getOrDefault(l2, domain.infinity());
				if (domain.less(newD, oldD)) {
					map.put(l2, newD);
					queue.add(new Pair<>(e.getFirst(),new Pair<>(l2,newD)));
				}				
			}
		}
	}

	public boolean checkDistance(int i, int j) {
		return bound.test(getDistance(i, j));
	}
	
	public <R> ArrayList<R> escape(SignalDomain<R> mDomain, Function<Integer, R> s) {
		HashMap<Integer, HashMap<Integer, R>> map = initEscapeMap(mDomain, s);
		LinkedList<Pair<Integer,Pair<Integer, R>>> queue = 
				IntStream
					.range(0, model.size()).boxed()
					.map(i -> new Pair<>(i,new Pair<>(i,s.apply(i))))
					.collect(Collectors.toCollection(LinkedList::new));
		while (!queue.isEmpty()) {
			Pair<Integer, Pair<Integer, R>> p = queue.poll();
			int l1 = p.getFirst();
			int l2 = p.getSecond().getFirst();
			R v = p.getSecond().getSecond();
			for (Pair<Integer, T> pre: model.previous(l1)) {
				int l = pre.getFirst();
				HashMap<Integer, R> m1 = map.get(l);
				R v1 = m1.getOrDefault(l2, mDomain.min());
				R newV = mDomain.disjunction(v1, mDomain.conjunction(s.apply(l), v));
				if (!v1.equals(newV)) {
					m1.put(l2, newV);
					queue.add(new Pair<>(l,new Pair<>(l2,newV)));
				}
			}
			
		}
		return extractEscapeValues(mDomain,map);		
	}
	
//	public <R> ArrayList<R> escape(SignalDomain<R> mDomain, Function<Integer, R> s) {
//		HashMap<Integer, HashMap<Integer, Pair<R, A>>> r = initEscapeMap( mDomain , s );		
//		Set<Integer> activeLocations = model.getLocations();
//		while (!activeLocations.isEmpty()) {
//			Set<Integer> newActive = new HashSet<>();
//			for (Integer l : activeLocations) {
//				HashMap<Integer, Pair<R, A>> lR = r.get(l);
//				for (Pair<Integer,T> p: model.previous(l)) {
//					HashMap<Integer, Pair<R, A>> rL1 = r.get(p.getFirst());
//					for (Entry<Integer, Pair<R, A>> ke : lR.entrySet()) {
//						A newB = domain.sum(distance.apply(p.getSecond()), ke.getValue().getSecond());
//						R newR = mDomain.conjunction(s.apply(ke.getKey()), ke.getValue().getFirst());
//						Pair<R,A> oldP = rL1.get(ke.getKey());
//						if (oldP == null) {
//							rL1.put(ke.getKey(), new Pair<>(newR,newB) );
//							newActive.add(ke.getKey());
//						} else {
//							Pair<R,A> newP = new Pair<>(
//									mDomain.disjunction(newR,oldP.getFirst()),
//									newB
//							);
//							if (!newP.equals(oldP)) {
//								rL1.put(ke.getKey(), newP);
//								newActive.add(ke.getKey());
//							}
//						}
//					}
//				}
//			}
//			activeLocations = newActive;			
//		}
//		return extractEscapeValues(mDomain,r);
//	}	
	
	public <R> ArrayList<R> reach(SignalDomain<R> mDomain, Function<Integer, R> s1, Function<Integer, R> s2) {
		HashMap<Integer,HashMap<Pair<Integer,R>,A>> r = initReachMap( mDomain , s2 );		
		Set<Integer> activeLocations = model.getLocations();
		while (!activeLocations.isEmpty()) {
			Set<Integer> newActive = new HashSet<>();
			for (Integer l : activeLocations) {
				HashMap<Pair<Integer, R>, A> lR = r.get(l);
				for (Pair<Integer,T> p: model.previous(l)) {
					HashMap<Pair<Integer, R>, A> rL1 = r.get(p.getFirst());
					for (Entry<Pair<Integer, R>, A> ke : lR.entrySet()) {
						A newB = domain.sum(distance.apply(p.getSecond()), ke.getValue());
						if (bound.test(newB)) {
							R newR = mDomain.conjunction(s1.apply(p.getFirst()), ke.getKey().getSecond());
							Pair<Integer,R> newP = new Pair<>(ke.getKey().getFirst(),newR);
							A d = rL1.get(newP);
							if (d == null) {
								rL1.put(newP, newB);
								newActive.add(p.getFirst());
							} else {
//								if (semiring.equals(d,newB)) {
//									rL1.put(newP, semiring.disjunction(d, newB));									
//									newActive.add(p.getFirst());
//								}
								throw new NullPointerException();
							}
						}
					}
				}
			}
			activeLocations = newActive;
		}
		return extractReachValues(mDomain,r);
	}

	private <R> ArrayList<R> extractReachValues(SignalDomain<R> mDomain, HashMap<Integer, HashMap<Pair<Integer, R>, A>> r) {
		ArrayList<R> toReturn = mDomain.createArray(model.size());
		for( int i=0 ; i<model.size() ; i++ ) {
			toReturn.set(i, mDomain.min());
			HashMap<Pair<Integer,R>,A> rI = r.get(i);
			for (Pair<Integer,R> k : rI.keySet()) {
				toReturn.set(i, mDomain.disjunction(toReturn.get(i), k.getSecond()));
			}
		}		
		return toReturn;
	}

	private <R> ArrayList<R> extractEscapeValues(SignalDomain<R> mDomain, HashMap<Integer, HashMap<Integer, R>> map) {
		ArrayList<R> toReturn = mDomain.createArray(model.size());
		for( int i=0 ; i<model.size() ; i++ ) {
			R value = mDomain.min();
			HashMap<Integer, R> mI = map.get(i);
			for (Entry<Integer, R> k : mI.entrySet()) {
				if (bound.test(getDistance(i, k.getKey()))) {
					value = mDomain.disjunction(value, k.getValue());
				}
			}
			toReturn.set(i,value);
		}		
		return toReturn;
	}

	private <R> HashMap<Integer, HashMap<Pair<Integer, R>, A>> initReachMap(SignalDomain<R> mDomain,
			Function<Integer, R> s2) {
		HashMap<Integer, HashMap<Pair<Integer, R>, A>> toReturn = new HashMap<>();
		for( int i=0 ; i<model.size() ; i++ ) {
			HashMap<Pair<Integer, R>, A> iR = new HashMap<>();
			iR.put(new Pair<Integer,R>(i,s2.apply(i)), domain.zero());
			toReturn.put(i, iR);
		}
		return toReturn;
	}

	private <R> HashMap<Integer, HashMap<Integer, R>> initEscapeMap(SignalDomain<R> mDomain,
			Function<Integer, R> s) {
		HashMap<Integer, HashMap<Integer, R>> toReturn = new HashMap<>();
		for( int i=0 ; i<model.size() ; i++ ) {
			HashMap<Integer, R> iR = new HashMap<>();
			iR.put(i,s.apply(i));
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
		for( int i=0 ; i<model.size() ; i++ ) {
			R v = dModule.max();
			for( int j=0 ; j<model.size(); j++ ) {
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
		for( int i=0 ; i<model.size() ; i++ ) {
			R v = dModule.min();
			for( int j=0 ; j<model.size(); j++ ) {
				if (this.checkDistance(i, j)) {
					v = dModule.disjunction(v, s.apply(j));
				}
			}
			values.set(i, v);
		}
		return values;
	}

	private <R> ArrayList<ArrayList<R>> createMatrix(int rows , int columns, BiFunction<Integer,Integer,R> init) {
		return IntStream
				.range(0, rows)
				.mapToObj(x -> createArray(columns,y -> init.apply(x, y)))
				.collect(Collectors.toCollection(ArrayList::new));
	}	

	private <R> ArrayList<R> createArray( int size , Function<Integer,R> init) {
		return IntStream
				.range(0, size)
				.mapToObj(x -> init.apply(x))
				.collect(Collectors.toCollection(ArrayList::new));
	}
	


}
