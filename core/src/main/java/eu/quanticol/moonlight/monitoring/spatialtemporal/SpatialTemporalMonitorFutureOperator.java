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

import java.util.function.BinaryOperator;

import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

import static eu.quanticol.moonlight.monitoring.temporal.TemporalMonitorFutureOperator.computeSignal;

/**
 * Strategy to interpret temporal logic operators on the future (except Until).
 *
 * @param <S> Spatial Graph Edge Type
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see SpatialTemporalMonitor
 */
public class SpatialTemporalMonitorFutureOperator<S, T, R>
		implements SpatialTemporalMonitor<S, T, R>
{
	private final SpatialTemporalMonitor<S, T, R> m;
	private final Interval interval;
	private final BinaryOperator<R> op;
	private final R init;

	public SpatialTemporalMonitorFutureOperator(SpatialTemporalMonitor<S, T, R> m,
												Interval interval,
												BinaryOperator<R> op,
												R init) {
		this.m = m;
		this.interval = interval;
		this.op = op;
		this.init = init;
	}

	@Override
	public SpatialTemporalSignal<R> monitor(LocationService<S> locationService,
											SpatialTemporalSignal<T> signal)
	{
		return m.monitor(locationService, signal).applyToSignal(
				s -> computeSignal(s, interval, op, init));
	}

}
