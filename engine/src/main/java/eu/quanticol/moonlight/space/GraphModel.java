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

package eu.quanticol.moonlight.space;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.util.Pair;

/**
 * Spatial model that implements a generic graph.
 *
 * @param <E> Type of edge labels
 *
 * @author loreti
 */
public class GraphModel<E> implements SpatialModel<E> {
	private final List<HashMap<Integer, E>> edges;
	private final List<List<Pair<Integer, E>>> outEdges;
	private final List<List<Pair<Integer, E>>> inEdges;

	private final int size;
	
	public GraphModel(int size) {
		this.size = size;
		edges = new ArrayList<>(size);
		outEdges = new ArrayList<>(size);
		inEdges = new ArrayList<>(size);
		init();
	}

	private void init() {
		for(int i = 0; i < size; i++) {
			edges.add(i, new HashMap<>());
			outEdges.add(i, new LinkedList<>());
			inEdges.add(i, new LinkedList<>());
		}
	}
	
	public void remove( int src, int trg ) {
		if (src >= size || trg >= size) {
			throw new ArrayIndexOutOfBoundsException("Unable to remove the edge");
		}

		if (!edges.get(src).containsKey(trg)) {
			throw new IllegalArgumentException("Edge not present!");
		}

		Pair<Integer, E> out = outEdges.get(src).get(trg);
		Pair<Integer, E> in = inEdges.get(src).get(trg);

		edges.get(src).remove(trg);
		outEdges.get(src).remove(out);
		inEdges.get(trg).remove(in);
	}

	public void add(int src, E value, int trg) {
		if (src == trg) {
			//throw new IllegalArgumentException(
			// "Self-loops are not allowed ("+src+"- "+value.toString()+" -> "+ trg+"!");
			return;//TODO: fix it!
		}
		//if (edges.get(src).containsKey(trg)) {
		//	throw new IllegalArgumentException("Duplicated edge!");
		//} TODO: check if the above test can be removed.
		edges.get(src).put(trg, value);
		outEdges.get(src).add(new Pair<>(trg, value));
		inEdges.get(trg).add(new Pair<>(src, value));
	}

	@Override
	public E get(int source, int target) {
		return edges.get(source).get(target);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public List<Pair<Integer, E>> next(int location) {
		return outEdges.get(location);
	}

	@Override
	public List<Pair<Integer, E>> previous(int location) {
		return inEdges.get(location);
	}

	@Override
	public Set<Integer> getLocations() {
		return IntStream.range(0, size).boxed().collect(Collectors.toSet());
	}

}
