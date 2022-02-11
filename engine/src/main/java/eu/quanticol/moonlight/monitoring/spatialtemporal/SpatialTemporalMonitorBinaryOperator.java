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

package eu.quanticol.moonlight.monitoring.spatialtemporal;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import eu.quanticol.moonlight.space.LocationService;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

/**
 * Strategy to interpret binary logic operators on the signal of interest.
 *
 * @param <S> Spatial Graph Edge Type
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see SpatialTemporalMonitor
 */
public class SpatialTemporalMonitorBinaryOperator<S, T, R>
		implements SpatialTemporalMonitor<S, T, R>
{

	private SpatialTemporalMonitor<S, T, R> m1;
	private BiFunction<R, R, R> op;
	private SpatialTemporalMonitor<S, T, R> m2;

	public SpatialTemporalMonitorBinaryOperator(
			SpatialTemporalMonitor<S, T, R> m1,
			BinaryOperator<R> op,
			SpatialTemporalMonitor<S, T, R> m2)
	{
		this.m1 = m1;
		this.op = op;
		this.m2 = m2;
	}

	@Override
	public SpatialTemporalSignal<R> monitor(LocationService<Double, S> locationService,
											SpatialTemporalSignal<T> signal)
	{
		return SpatialTemporalSignal.apply(m1.monitor(locationService, signal),
										   op,
										   m2.monitor(locationService, signal));
	}

}
