/**
 * 
 */
package eu.quanticol.moonlight.formula;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author loreti
 *
 */
public interface Semiring<R> {
	
	public R conjunction(R x, R y);

	public R disjunction(R x, R y);
	
	public R min();
	
	public R max();
	
	public default ArrayList<R> createArray( int size ) {
		return createArray(size, i -> min());
	}

	public default ArrayList<ArrayList<R>> createMatrix(int rows , int columns) {
		return createMatrix( rows, columns, (x,y) -> min() );
	}

	public default ArrayList<R> createArray( int size , Function<Integer,R> init) {
		return IntStream
				.range(0, size)
				.mapToObj(x -> init.apply(x))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	public default ArrayList<ArrayList<R>> createMatrix(int rows , int columns, BiFunction<Integer,Integer,R> init) {
		return IntStream
				.range(0, rows)
				.mapToObj(x -> createArray(columns,y -> init.apply(x, y)))
				.collect(Collectors.toCollection(ArrayList::new));
	}	

}
