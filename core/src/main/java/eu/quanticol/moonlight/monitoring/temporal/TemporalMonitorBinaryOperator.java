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

package eu.quanticol.moonlight.monitoring.temporal;

import eu.quanticol.moonlight.signal.Signal;
import java.util.function.BinaryOperator;

/**
 * Strategy to interpret a binary logic operator
 *
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see TemporalMonitor
 */
public class TemporalMonitorBinaryOperator<T, R>
		implements TemporalMonitor<T, R>
{
	private final TemporalMonitor<T, R> m1;
	private final BinaryOperator<R> op;
	private final TemporalMonitor<T, R> m2;

	public TemporalMonitorBinaryOperator(TemporalMonitor<T, R> m1,
										 BinaryOperator<R> op,
										 TemporalMonitor<T, R> m2)
	{
		this.m1 = m1;
		this.op = op;
		this.m2 = m2;
	}

	@Override
	public Signal<R> monitor(Signal<T> signal) {
		return Signal.apply(m1.monitor(signal), op, m2.monitor(signal));
	}

}
