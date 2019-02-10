/**
 * 
 */
package eu.quanticol.moonlight.signal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import eu.quanticol.moonlight.formula.DomainModule;
import eu.quanticol.moonlight.util.Pair;

/**
 * @author loreti
 *
 */
public class DistanceStructure<T,A> {
	
	private final BiFunction<T,A,A> distance;
	
	private final DomainModule<A> domain;
	
	private final Predicate<A> bound;
	
	private final SpatialModel<T> model;
	
	private A[][] distanceMatrix;
	
	/**
	 * @param distance
	 * @param domain
	 * @param guard
	 * @param model
	 */
	public DistanceStructure(BiFunction<T, A, A> distance, DomainModule<A> domain, Predicate<A> bound,
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
		return distanceMatrix[i][j];
	}

	private void computeDistanceMatrix() {
		// TODO Auto-generated method stub
		
	}

	public boolean checkDistance(int i, int j) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public <R> R[] escape(DomainModule<R> mDomain, Function<Integer, R> s) {
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
									domain.disjunction(newB, oldP.getSecond())
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
	
	public <R> R[] reach(DomainModule<R> mDomain, Function<Integer, R> s1, Function<Integer, R> s2) {
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
									rL1.put(newP, domain.disjunction(d, newB));									
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

	private <R> R[] extractReachValues(DomainModule<R> mDomain, HashMap<Integer, HashMap<Pair<Integer, R>, A>> r) {
		R[] toReturn = mDomain.createArray(model.size());
		for( int i=0 ; i<model.size() ; i++ ) {
			toReturn[i] = mDomain.min();
			HashMap<Pair<Integer,R>,A> rI = r.get(i);
			for (Pair<Integer,R> k : rI.keySet()) {
				toReturn[i] = mDomain.disjunction(toReturn[i], k.getSecond());
			}
		}		
		return toReturn;
	}

	private <R> R[] extractEscapeValues(DomainModule<R> mDomain, HashMap<Integer, HashMap<Integer, Pair<R, A>>> r) {
		R[] toReturn = mDomain.createArray(model.size());
		for( int i=0 ; i<model.size() ; i++ ) {
			toReturn[i] = mDomain.min();
			HashMap<Integer,Pair<R,A>> rI = r.get(i);
			for (Pair<R, A> k : rI.values()) {
				if (bound.test(k.getSecond())) {
					toReturn[i] = mDomain.disjunction(toReturn[i], k.getFirst());
				}
			}
		}		
		return toReturn;
	}

	private <R> HashMap<Integer, HashMap<Pair<Integer, R>, A>> initReachMap(DomainModule<R> mDomain,
			Function<Integer, R> s2) {
		HashMap<Integer, HashMap<Pair<Integer, R>, A>> toReturn = new HashMap<>();
		for( int i=0 ; i<model.size() ; i++ ) {
			HashMap<Pair<Integer, R>, A> iR = new HashMap<>();
			iR.put(new Pair<Integer,R>(i,s2.apply(i)), domain.min());
			toReturn.put(i, iR);
		}
		return toReturn;
	}

	private <R> HashMap<Integer, HashMap<Integer, Pair<R, A>>> initEscapeMap(DomainModule<R> mDomain,
			Function<Integer, R> s2) {
		HashMap<Integer, HashMap<Integer, Pair<R, A>>> toReturn = new HashMap<>();
		for( int i=0 ; i<model.size() ; i++ ) {
			HashMap<Integer, Pair<R, A>> iR = new HashMap<>();
			iR.put(i,new Pair<R,A>(s2.apply(i), domain.min()));
			toReturn.put(i, iR);
		}
		return toReturn;
	}

	public <R> R[] everywhere(DomainModule<R> dModule, Function<Integer, R> s) {
		R[] values = dModule.createArray(model.size());
		for( int i=0 ; i<model.size() ; i++ ) {
			R v = dModule.max();
			for( int j=0 ; j<model.size(); j++ ) {
				if (this.checkDistance(i, j)) {
					v = dModule.conjunction(v, s.apply(j));
				}
			}
			values[i] = v;
		}
		return values;
	}
	
	public <R> R[] somewhere(DomainModule<R> dModule, Function<Integer, R> s) {
		R[] values = dModule.createArray(model.size());
		for( int i=0 ; i<model.size() ; i++ ) {
			R v = dModule.min();
			for( int j=0 ; j<model.size(); j++ ) {
				if (this.checkDistance(i, j)) {
					v = dModule.disjunction(v, s.apply(j));
				}
			}
			values[i] = v;
		}
		return values;
	}


	


}
