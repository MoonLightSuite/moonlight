/**
 * 
 */
package eu.quanticol.moonlight.signal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import eu.quanticol.moonlight.formula.Semiring;
import eu.quanticol.moonlight.formula.SignalDomain;
import eu.quanticol.moonlight.util.Pair;

/**
 * @author loreti
 *
 */
public class DistanceStructure<T,A> {
	
	private final BiFunction<T,A,A> distance;
	
	private final Semiring<A> semiring;
	
	private final Predicate<A> bound;
	
	private final SpatialModel<T> model;
	
	private ArrayList<ArrayList<A>> distanceMatrix;
	
	/**
	 * @param distance
	 * @param domain
	 * @param guard
	 * @param model
	 */
	public DistanceStructure(BiFunction<T, A, A> distance, Semiring<A> domain, Predicate<A> bound,
			SpatialModel<T> model) {
		super();
		this.distance = distance;
		this.semiring = domain;
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
			return semiring.min();
		} else {
			T e = model.get(i, j);
			if (e == null) {
				return semiring.max();
			} else {
				return distance.apply(e,semiring.min());
			}
		}
	}
	
	private void computeDistanceMatrix() {
		distanceMatrix = semiring.createMatrix(model.size(),model.size(),this::get);
		boolean stable = false;
		while (!stable) {
			stable = true;
			for( int i=0 ; i<model.size() ; i++ ) {
				for (int j=0 ; j<model.size() ; j++ ) {
					if (i != j) {
						for (int k=0 ; k<model.size() ; k++ ) {
							if ((k!=i)&&(k!=j)) {
								A newD = semiring.disjunction(distanceMatrix.get(i).get(j), semiring.conjunction(distanceMatrix.get(i).get(k), distanceMatrix.get(k).get(j)));
								if (!newD.equals(distanceMatrix.get(i).get(j)) ) {
									distanceMatrix.get(i).set(j, newD);
									stable = false;
								}
							}
						}
					}
				}
			}
		}
	}

	public boolean checkDistance(int i, int j) {
		return bound.test(getDistance(i, j));
	}
	
	public <R> ArrayList<R> escape(SignalDomain<R> mDomain, Function<Integer, R> s) {
		HashMap<Integer, HashMap<Integer, Pair<R, A>>> r = initEscapeMap( mDomain , s );		
		Set<Integer> activeLocations = model.getLocations();
		while (!activeLocations.isEmpty()) {
			Set<Integer> newActive = new HashSet<>();
			for (Integer l : activeLocations) {
				HashMap<Integer, Pair<R, A>> lR = r.get(l);
				for (Pair<Integer,T> p: model.previous(l)) {
					HashMap<Integer, Pair<R, A>> rL1 = r.get(p.getFirst());
					for (Entry<Integer, Pair<R, A>> ke : lR.entrySet()) {
						A newB = distance.apply(p.getSecond(), ke.getValue().getSecond());
						R newR = mDomain.conjunction(s.apply(ke.getKey()), ke.getValue().getFirst());
						Pair<R,A> oldP = rL1.get(ke.getKey());
						if (oldP == null) {
							rL1.put(ke.getKey(), new Pair<>(newR,newB) );
							newActive.add(ke.getKey());
						} else {
							Pair<R,A> newP = new Pair<>(
									mDomain.disjunction(newR,oldP.getFirst()),
									semiring.disjunction(newB, oldP.getSecond())
							);
							if (!newP.equals(oldP)) {
								rL1.put(ke.getKey(), newP);
								newActive.add(ke.getKey());
							}
						}
					}
				}
			}
			
		}
		return extractEscapeValues(mDomain,r);
	}	
	
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
						A newB = distance.apply(p.getSecond(), ke.getValue());
						if (bound.test(newB)) {
							R newR = mDomain.conjunction(s1.apply(p.getFirst()), ke.getKey().getSecond());
							Pair<Integer,R> newP = new Pair<>(ke.getKey().getFirst(),newR);
							A d = rL1.get(newP);
							if (d == null) {
								rL1.put(newP, newB);
								newActive.add(p.getFirst());
							} else {
								if (!d.equals(newB)) {
									rL1.put(newP, semiring.disjunction(d, newB));									
									newActive.add(p.getFirst());
								}
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

	private <R> ArrayList<R> extractEscapeValues(SignalDomain<R> mDomain, HashMap<Integer, HashMap<Integer, Pair<R, A>>> r) {
		ArrayList<R> toReturn = mDomain.createArray(model.size());
		for( int i=0 ; i<model.size() ; i++ ) {
			toReturn.set(i,mDomain.min());
			HashMap<Integer,Pair<R,A>> rI = r.get(i);
			for (Pair<R, A> k : rI.values()) {
				if (bound.test(k.getSecond())) {
					toReturn.set(i,mDomain.disjunction(toReturn.get(i), k.getFirst()));
				}
			}
		}		
		return toReturn;
	}

	private <R> HashMap<Integer, HashMap<Pair<Integer, R>, A>> initReachMap(SignalDomain<R> mDomain,
			Function<Integer, R> s2) {
		HashMap<Integer, HashMap<Pair<Integer, R>, A>> toReturn = new HashMap<>();
		for( int i=0 ; i<model.size() ; i++ ) {
			HashMap<Pair<Integer, R>, A> iR = new HashMap<>();
			iR.put(new Pair<Integer,R>(i,s2.apply(i)), semiring.min());
			toReturn.put(i, iR);
		}
		return toReturn;
	}

	private <R> HashMap<Integer, HashMap<Integer, Pair<R, A>>> initEscapeMap(SignalDomain<R> mDomain,
			Function<Integer, R> s2) {
		HashMap<Integer, HashMap<Integer, Pair<R, A>>> toReturn = new HashMap<>();
		for( int i=0 ; i<model.size() ; i++ ) {
			HashMap<Integer, Pair<R, A>> iR = new HashMap<>();
			iR.put(i,new Pair<R,A>(s2.apply(i), semiring.min()));
			toReturn.put(i, iR);
		}
		return toReturn;
	}

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


	


}
