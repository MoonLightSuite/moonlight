/**
 * 
 */
package eu.quanticol.moonlight.signal;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import eu.quanticol.moonlight.formula.DomainModule;

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
	

	

}
