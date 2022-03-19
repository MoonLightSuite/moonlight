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

package eu.quanticol.moonlight.offline.monitoring.temporal;

import java.util.function.BinaryOperator;

import static eu.quanticol.moonlight.offline.algorithms.TemporalOp.computeFutureSignal;
import eu.quanticol.moonlight.core.formula.Interval;
import eu.quanticol.moonlight.offline.signal.Signal;

/**
 * Strategy to interpret temporal logic operators on the future (except Until)
 *
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see TemporalMonitor
 */
public class TemporalMonitorFutureOperator<T, R>
		implements TemporalMonitor<T, R>
{
	private final TemporalMonitor<T, R> m;
	private final BinaryOperator<R>  op;
	private final R init;
	private final Interval interval;

	public TemporalMonitorFutureOperator(TemporalMonitor<T, R> m,
										 BinaryOperator<R> op, R init,
										 Interval interval)
	{
		this.m = m;
		this.op = op;
		this.init = init;
		this.interval = interval;
	}

	public TemporalMonitorFutureOperator(TemporalMonitor<T, R> m,
										 BinaryOperator<R> op, R init)
	{
		this(m, op, init, null);
	}

	@Override
	public Signal<R> monitor(Signal<T> signal) {
		return computeFutureSignal(m.monitor(signal), interval, op, init);
	}

}
