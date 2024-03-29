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
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.moonlightsuite.moonlight.space;

import io.github.moonlightsuite.moonlight.core.space.SpatialModel;
import io.github.moonlightsuite.moonlight.core.base.Pair;

import java.util.*;

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
	}

	private void init() {
		for(int i = 0; i<this.locations; i++ ) {
			this.edges.add(i, new HashMap<>());
			this.outEdges.add(i, new LinkedList<>());
			this.inEdges.add(i, new LinkedList<>());
		}
	}

	public ImmutableGraphModel<T> remove(int src, int trg ) {
		if (src >= locations || trg >= locations) {
			throw new ArrayIndexOutOfBoundsException("Unable to remove the edge");
		}

		if (!this.edges.get(src).containsKey(trg)) {
			throw new IllegalArgumentException("Edge not present!");
		}

		return generateFromRemoval(src, trg);
	}

	public ImmutableGraphModel<T> add(int src, T value , int trg ) {
		if (src == trg) {
			throw new IllegalArgumentException("Self-loops are not allowed!");
		}
		if (this.edges.get(src).containsKey(trg)) {
			throw new IllegalArgumentException("Duplicated edge!");
		}

		return generateFromAddition(src, value, trg);
	}

	@Override
	public T get(int source, int target) {
		return this.edges.get(source).get(target);
	}

	@Override
	public int size() {
		return locations;
	}

	@Override
	public List<Pair<Integer, T>> next(int location) {
		return this.outEdges.get(location);
	}

	@Override
	public List<Pair<Integer, T>> previous(int location) {
		return this.inEdges.get(location);
	}

	private ImmutableGraphModel<T> generateFromRemoval(int src, int trg) {
		Pair<Integer,T> out = outEdges.get(src).get(trg);
		Pair<Integer,T> in = inEdges.get(src).get(trg);

		// create new edges...
		List<HashMap<Integer,T>> newEdges = new ArrayList<>(edges);
		newEdges.set(src, new HashMap<>(edges.get(src)));
		newEdges.get(src).remove(trg);

		// create new inEdges...
		List<List<Pair<Integer,T>>> newInEdges = new ArrayList<>(inEdges);
		newInEdges.set(trg, new ArrayList<>(trg));
		newInEdges.get(trg).remove(in);

		// create new outEdges...
		List<List<Pair<Integer,T>>> newOutEdges = new ArrayList<>(outEdges);
		newOutEdges.set(src, new ArrayList<>(src));
		newOutEdges.get(src).remove(out);

		return new ImmutableGraphModel<>(locations, newEdges, newInEdges, newOutEdges);
	}

	private ImmutableGraphModel<T> generateFromAddition(int src, T value, int trg) {

		// create new edges...
		List<HashMap<Integer,T>> newEdges = new ArrayList<>(edges);
		newEdges.set(src, new HashMap<>(edges.get(src)));
		newEdges.get(src).put(trg, value);

		// create new inEdges...
		List<List<Pair<Integer,T>>> newInEdges = new ArrayList<>(inEdges);
		newInEdges.set(trg, new ArrayList<>(inEdges.get(trg)));
		newInEdges.get(trg).add(new Pair<>(src, value));

		// create new outEdges...
		List<List<Pair<Integer,T>>> newOutEdges = new ArrayList<>(outEdges);
		newOutEdges.set(src, new ArrayList<>(outEdges.get(src)));
		newOutEdges.get(src).add(new Pair<>(trg, value));

		return new ImmutableGraphModel<>(locations, newEdges, newInEdges, newOutEdges);
	}

}
