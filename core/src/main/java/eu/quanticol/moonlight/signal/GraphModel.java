/**
 * 
 */
package eu.quanticol.moonlight.signal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import eu.quanticol.moonlight.util.Pair;

/**
 * @author loreti
 *
 */
public class GraphModel<T> implements SpatialModel<T> {

	private final ArrayList<HashMap<Integer,T>> edges;
	
	private final ArrayList<List<Pair<Integer,T>>> outEdges;
	
	private final ArrayList<List<Pair<Integer,T>>> inEdges;

	private int size;
	
	public GraphModel( int size ) {
		this.size = size;
		this.edges = new ArrayList<>(size);
		this.outEdges = new ArrayList<>(size);
		this.inEdges = new ArrayList<>(size);
		init();
	}
	
	
	private void init() {
		for( int i=0 ; i<this.size ; i++ ) {
			this.edges.add(i, new HashMap<>());
			this.outEdges.add(i, new LinkedList<>());
			this.inEdges.add(i, new LinkedList<>());
		}
	}
	
	public void add( int src, T value , int trg ) {
		if (src == trg) {
			throw new IllegalArgumentException("Self-loops are not allowed!");
		}
		if (this.edges.get(src).containsKey(trg)) {
			throw new IllegalArgumentException("Duplicated edge!");
		}
		this.edges.get(src).put(trg,value);
		this.outEdges.get(src).add(new Pair<Integer,T>(trg,value));
		this.inEdges.get(trg).add(new Pair<Integer,T>(src,value));
	}


	@Override
	public T get(int src, int trg) {
		return this.edges.get(src).get(trg);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public List<Pair<Integer, T>> next(int l) {
		return this.outEdges.get(l);
	}

	@Override
	public List<Pair<Integer, T>> previous(int l) {
		return this.inEdges.get(l);
	}

	@Override
	public Set<Integer> getLocations() {
		return IntStream.range(0,size).boxed().collect(Collectors.toSet());
	}

}
