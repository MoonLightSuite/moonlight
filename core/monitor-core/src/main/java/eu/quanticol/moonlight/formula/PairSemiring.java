/**
 * 
 */
package eu.quanticol.moonlight.formula;

import eu.quanticol.moonlight.util.Pair;

/**
 * @author loreti
 *
 */
public class PairSemiring<T,R> implements Semiring<Pair<T,R>> {
	
	private final Semiring<T> firstSemiring;
	private final Semiring<R> secondSemiring;
	private final Pair<T,R> min;
	private final Pair<T,R> max;
	
	public PairSemiring( Semiring<T> firstSemiring, Semiring<R> secondSemiring) {
		this.firstSemiring = firstSemiring;
		this.secondSemiring = secondSemiring;
		this.min = new Pair<>(firstSemiring.min(),secondSemiring.min());
		this.max = new Pair<>(firstSemiring.max(),secondSemiring.max());
	}

	@Override
	public Pair<T, R> conjunction(Pair<T, R> x, Pair<T, R> y) {
		return new Pair<>( 
			firstSemiring.conjunction(x.getFirst(), y.getFirst()),
			secondSemiring.conjunction(x.getSecond(), y.getSecond())	
		);
	}

	@Override
	public Pair<T, R> disjunction(Pair<T, R> x, Pair<T, R> y) {
		return new Pair<>( 
				firstSemiring.disjunction(x.getFirst(), y.getFirst()),
				secondSemiring.disjunction(x.getSecond(), y.getSecond())	
			);
	}

	@Override
	public Pair<T, R> min() {
		return this.min;
	}

	@Override
	public Pair<T, R> max() {
		return this.max;
	}

}
