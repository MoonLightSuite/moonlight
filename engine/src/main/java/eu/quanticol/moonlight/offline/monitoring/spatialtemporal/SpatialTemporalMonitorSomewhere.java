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
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.quanticol.moonlight.offline.monitoring.spatialtemporal;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

import eu.quanticol.moonlight.core.algorithms.SpatialAlgorithms;
import eu.quanticol.moonlight.offline.algorithms.SpatialComputation;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;

/**
 * Strategy to interpret the Somewhere spatial logic operator.
 *
 * @param <S> Spatial Graph Edge Type
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see SpatialTemporalMonitor
 */
public class SpatialTemporalMonitorSomewhere<S, T, R> implements SpatialTemporalMonitor<S, T, R> {

	private final SpatialTemporalMonitor<S, T, R> m;
	private final Function<SpatialModel<S>, DistanceStructure<S, ?>> distance;
	private final SignalDomain<R> domain;

	public SpatialTemporalMonitorSomewhere(SpatialTemporalMonitor<S, T, R> m,
                                           Function<SpatialModel<S>, DistanceStructure<S, ?>> distance,
                                           SignalDomain<R> domain)
    {
		this.m = m;
		this.distance = distance;
		this.domain = domain;
	}

	@Override
	public SpatialTemporalSignal<R> monitor(LocationService<Double, S> locationService, SpatialTemporalSignal<T> signal) {
		SpatialComputation<S, R> sp = new SpatialComputation<>(locationService,
				distance,
				this::somewhereOp);
        return sp.computeUnary(m.monitor(locationService, signal));
	}

    private List<R> somewhereOp(IntFunction<R> spatialSignal,
                                 DistanceStructure<S, ?> ds)
    {
        return SpatialAlgorithms.somewhere(domain, spatialSignal, ds);
    }
}
