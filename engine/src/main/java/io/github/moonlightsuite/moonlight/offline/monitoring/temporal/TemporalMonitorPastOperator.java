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

package io.github.moonlightsuite.moonlight.offline.monitoring.temporal;

import java.util.function.BinaryOperator;

import static io.github.moonlightsuite.moonlight.offline.algorithms.TemporalOp.computePastSignal;
import io.github.moonlightsuite.moonlight.core.formula.Interval;
import io.github.moonlightsuite.moonlight.offline.signal.Signal;

/**
 * Strategy to interpret temporal logic operators on the past (except Since)
 *
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see TemporalMonitor
 */
public class TemporalMonitorPastOperator<T, R> implements TemporalMonitor<T, R> {

	private final TemporalMonitor<T, R> m;
	private final BinaryOperator<R> op;
	private final R init;
	private final Interval interval;

	public TemporalMonitorPastOperator(TemporalMonitor<T, R> m,
									   BinaryOperator<R> op, R init,
									   Interval interval)
	{
		this.m = m;
		this.op = op;
		this.init = init;
		this.interval = interval;
	}

	public TemporalMonitorPastOperator(TemporalMonitor<T, R> m,
									   BinaryOperator<R> op, R min)
	{
		this(m, op, min, null);
	}

	@Override
	public Signal<R> monitor(Signal<T> signal) {
		return computePastSignal(m.monitor(signal), interval, op, init);
	}

}
