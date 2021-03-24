/*
 * MoonLight: a light-weight framework for runtime monitoring
 * Copyright (C) 2018
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.quanticol.moonlight.monitoring.online.strategy.spacetime;

import eu.quanticol.moonlight.algorithms.online.SpatialComputation;
import eu.quanticol.moonlight.domain.AbsIntervalDomain;
import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.monitoring.online.strategy.time.OnlineMonitor;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.space.DistanceStructure;
import eu.quanticol.moonlight.signal.space.LocationService;
import eu.quanticol.moonlight.signal.space.SpatialModel;
import eu.quanticol.moonlight.signal.online.OnlineSpaceTimeSignal;
import eu.quanticol.moonlight.signal.online.SignalInterface;
import eu.quanticol.moonlight.signal.online.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Strategy to interpret the Somewhere spatial logic operator.
 *
 * @param <S> Spatial Graph Edge Type
 * @param <V> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see SpatialTemporalMonitor
 */
public class SomewhereMonitor<S, V, R extends Comparable<R>>
        implements OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>>
{
	private final OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> argument;
	private final Function<SpatialModel<S>, DistanceStructure<S, ?>> distance;
	private final SignalDomain<AbstractInterval<R>> domain;
	private final SignalInterface<Double, List<AbstractInterval<R>>> rho;
	private final LocationService<S> locationService;

	public SomewhereMonitor(
			OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> argument,
			int locations,
			LocationService<S> locationService,
			Function<SpatialModel<S>, DistanceStructure<S, ?>> distance,
			SignalDomain<R> domain)
    {
    	this.locationService = locationService;
		this.argument = argument;
		this.distance = distance;
		this.rho = new OnlineSpaceTimeSignal<>(locations, domain);
		this.domain = new AbsIntervalDomain<>(domain);
	}



	@Override
	public List<Update<Double, List<AbstractInterval<R>>>> monitor(
			Update<Double, List<V>> signalUpdate)
	{
		List<Update<Double, List<AbstractInterval<R>>>> argUpdates =
												argument.monitor(signalUpdate);

		List<Update<Double, List<AbstractInterval<R>>>> updates = new ArrayList<>();

		Function<Update<Double, List<AbstractInterval<R>>>,
				List<Update<Double, List<AbstractInterval<R>>>>>
				f = u ->
				SpatialComputation
						.computeDynamic(locationService,
										distance,
										this::somewhereOp,
										u,
										argument.getResult().getSegments());

		for(Update<Double, List<AbstractInterval<R>>> argU : argUpdates) {
			updates.addAll(f.apply(argU));
		}

		for(Update<Double, List<AbstractInterval<R>>> u : updates) {
			rho.refine(u);
		}

		return updates;
	}

	@Override
	public SignalInterface<Double, List<AbstractInterval<R>>> getResult() {
		return rho;
	}


	private List<AbstractInterval<R>> somewhereOp(
			Function<Integer, AbstractInterval<R>> spatialSignal,
			DistanceStructure<S, ?> ds)
	{
		return DistanceStructure.somewhere(domain, spatialSignal, ds);
	}
}
