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

package eu.quanticol.moonlight.monitoring.spatialtemporal;

import java.util.List;
import java.util.function.Function;

import eu.quanticol.moonlight.algorithms.SpaceOperator;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.signal.space.DistanceStructure;
import eu.quanticol.moonlight.signal.space.LocationService;
import eu.quanticol.moonlight.signal.space.SpatialModel;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

/**
 * Strategy to interpret the Everywhere spatial logic operator.
 *
 * @param <S> Spatial Graph Edge Type
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see SpatialTemporalMonitor
 */
public class SpatialTemporalMonitorEverywhere<S, T, R>
        implements SpatialTemporalMonitor<S, T, R>
{
	private final SpatialTemporalMonitor<S, T, R> m;
	private final Function<SpatialModel<S>, DistanceStructure<S, ?>> distance;
	private final SignalDomain<R> domain;

	public SpatialTemporalMonitorEverywhere(SpatialTemporalMonitor<S, T, R> m,
											Function<SpatialModel<S>,
                                            DistanceStructure<S, ?>> distance,
											SignalDomain<R> domain)
    {
		this.m = m;
		this.distance = distance;
		this.domain = domain;
	}

	@Override
	public SpatialTemporalSignal<R> monitor(LocationService<S> locationService,
                                            SpatialTemporalSignal<T> signal)
    {
		return SpaceOperator.computeWhereDynamic(locationService,
                                            distance,
                                            this::everywhereOp,
                                            m.monitor(locationService, signal));
	}

    private List<R> everywhereOp(Function<Integer, R> spatialSignal,
								 DistanceStructure<S, ?> ds)
    {
	    return DistanceStructure.everywhere(domain, spatialSignal, ds);
    }
}
