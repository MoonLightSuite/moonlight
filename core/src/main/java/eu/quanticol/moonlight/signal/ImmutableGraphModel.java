/**
 * 
 */
package eu.quanticol.moonlight.signal;

import eu.quanticol.moonlight.util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Immutable version of the GraphModel class.
 * This is required (or advisable) in order to support future versions of the
 * algorithms allowing any of the followings:
 *
 * - parallel monitoring of different properties
 * - multi-threaded monitoring of a property
 * - online monitoring
 *
 */
public class ImmutableGraphModel<T> implements SpatialModel<T> {

	private final List<HashMap<Integer,T>> edges;

	private final List<List<Pair<Integer,T>>> outEdges;

	private final List<List<Pair<Integer,T>>> inEdges;

	/**
	 * Number of locations of the graph
	 */
	private final int locations;

	public ImmutableGraphModel(int size ) {
		this.locations = size;
		this.edges = new ArrayList<>(size);
		this.outEdges = new ArrayList<>(size);
		this.inEdges = new ArrayList<>(size);
		init();
	}

	private ImmutableGraphModel(int locations,
								List<HashMap<Integer,T>> edges,
								List<List<Pair<Integer,T>>> inEdges,
								List<List<Pair<Integer,T>>> outEdges ) {
		this.locations = locations;
		this.edges = edges;
		this.inEdges = inEdges;
		this.outEdges = outEdges;
		init();
	}
	
	private void init() {
		for(int i = 0; i<this.locations; i++ ) {
			this.edges.add(i, new HashMap<>());
			this.outEdges.add(i, new LinkedList<>());
			this.inEdges.add(i, new LinkedList<>());
		}
	}
	
	public ImmutableGraphModel<T> remove( int src, int trg ) {
		if (src >= locations || trg >= locations) {
			throw new ArrayIndexOutOfBoundsException("Unable to remove the edge");
		}

		if (!this.edges.get(src).containsKey(trg)) {
			throw new IllegalArgumentException("Edge not present!");
		}

		return generateFromRemoval(src, trg);
	}

	public ImmutableGraphModel<T> add( int src, T value , int trg ) {
		if (src == trg) {
			throw new IllegalArgumentException("Self-loops are not allowed!");
		}
		if (this.edges.get(src).containsKey(trg)) {
			throw new IllegalArgumentException("Duplicated edge!");
		}

		return generateFromAddition(src, value, trg);
	}

	@Override
	public T get(int src, int trg) {
		return this.edges.get(src).get(trg);
	}

	@Override
	public int size() {
		return locations;
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
		return IntStream.range(0, locations).boxed().collect(Collectors.toSet());
	}

	private ImmutableGraphModel<T> generateFromRemoval( int src, int trg) {
		Pair<Integer,T> out = this.outEdges.get(src).get(trg);
		Pair<Integer,T> in = this.inEdges.get(src).get(trg);

		// create new edges...
		List<HashMap<Integer,T>> newEdges = new ArrayList<>(this.edges);
		newEdges.set(src, new HashMap<>(this.edges.get(src)));
		newEdges.get(src).remove(trg);

		// create new inEdges...
		List<List<Pair<Integer,T>>> newInEdges = new ArrayList<>(this.inEdges);
		newInEdges.set(trg, new ArrayList<>(trg));
		newInEdges.get(trg).remove(in);

		// create new outEdges...
		List<List<Pair<Integer,T>>> newOutEdges = new ArrayList<>(this.outEdges);
		newOutEdges.set(src, new ArrayList<>(src));
		newOutEdges.get(src).remove(out);

		return new ImmutableGraphModel<>(locations, newEdges, newInEdges, newOutEdges);
	}

	private ImmutableGraphModel<T> generateFromAddition(int src, T value, int trg) {

		// create new edges...
		List<HashMap<Integer,T>> newEdges = new ArrayList<>(this.edges);
		newEdges.set(src, new HashMap<>(this.edges.get(src)));
		newEdges.get(src).put(trg, value);

		// create new inEdges...
		List<List<Pair<Integer,T>>> newInEdges = new ArrayList<>(this.inEdges);
		newInEdges.set(trg, new ArrayList<>(trg));
		newInEdges.get(trg).add(new Pair<>(src,value));

		// create new outEdges...
		List<List<Pair<Integer,T>>> newOutEdges = new ArrayList<>(this.outEdges);
		newOutEdges.set(src, new ArrayList<>(src));
		newOutEdges.get(src).add(new Pair<>(trg,value));

		return new ImmutableGraphModel<>(locations, newEdges, newInEdges, newOutEdges);
	}

}
