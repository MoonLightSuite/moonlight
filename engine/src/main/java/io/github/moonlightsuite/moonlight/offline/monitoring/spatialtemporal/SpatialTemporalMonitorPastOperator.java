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

package io.github.moonlightsuite.moonlight.offline.monitoring.spatialtemporal;

import java.util.function.BinaryOperator;

import io.github.moonlightsuite.moonlight.core.formula.Interval;
import io.github.moonlightsuite.moonlight.core.space.LocationService;
import io.github.moonlightsuite.moonlight.offline.algorithms.TemporalOp;
import io.github.moonlightsuite.moonlight.offline.signal.SpatialTemporalSignal;

/**
 * Strategy to interpret temporal logic operators on the past (except Since).
 *
 * @param <S> Spatial Graph Edge Type
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see SpatialTemporalMonitor
 */
public class SpatialTemporalMonitorPastOperator<S, T, R>
		implements SpatialTemporalMonitor<S, T, R>
{
	private final SpatialTemporalMonitor<S, T, R> m;
	private final Interval interval;
	private final BinaryOperator<R> op;
	private final R init;

	public SpatialTemporalMonitorPastOperator(SpatialTemporalMonitor<S, T, R> m,
											  Interval interval,
											  BinaryOperator<R> op,
											  R init)
	{
		this.m = m;
		this.interval = interval;
		this.op = op;
		this.init = init;
	}

	@Override
	public SpatialTemporalSignal<R> monitor(LocationService<Double, S> locationService,
											SpatialTemporalSignal<T> signal)
	{
		return m.monitor(locationService, signal).applyToSignal(
				s -> TemporalOp.computePastSignal(s, interval, op,init));
	}

}
