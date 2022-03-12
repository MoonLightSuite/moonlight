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

import java.util.Iterator;
import java.util.LinkedList;

import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.core.base.Pair;

/**
 * @author loreti
 *
 */
public class LocationServiceList<V> implements LocationService<Double, V> {
	
	private final LinkedList<Pair<Double, SpatialModel<V>>> steps = new LinkedList<>();
	private Pair<Double,SpatialModel<V>> last;
	
	public LocationServiceList() {

	}
	
	public void add( double t, SpatialModel<V> m) {
		if ((last==null)||(last.getFirst()<t)) {
			last = new Pair<>(t,m);
			steps.add(last);
		} else {
			throw new IllegalArgumentException("Wrong time! Is "+t+" expexted >"+last.getFirst()+"!");
		}
	}

	@Override
	public SpatialModel<V> get(Double t) {
		 Pair<Double,SpatialModel<V>> temp = null;
		 for (Pair<Double, SpatialModel<V>> p : steps) {
			if ((temp!=null)&&(t<p.getFirst())) {
				return temp.getSecond();
			}
			temp = p;
		}
		return (temp!=null?temp.getSecond():null);
	}

	@Override
	public Iterator<Pair<Double, SpatialModel<V>>> times() {
		return steps.iterator();
	}

	@Override
	public boolean isEmpty() {
		return steps.isEmpty();
	}

}
