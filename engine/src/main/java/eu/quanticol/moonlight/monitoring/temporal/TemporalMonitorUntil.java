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

import eu.quanticol.moonlight.algorithms.UntilOperator;
import eu.quanticol.moonlight.core.formula.Interval;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.signal.Signal;

/**
 * Strategy to interpret the Until operator
 *
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see TemporalMonitor
 */
public class TemporalMonitorUntil<T, R> implements TemporalMonitor<T, R> {

	private final TemporalMonitor<T, R> m1;
	private final Interval interval;
	private final TemporalMonitor<T, R> m2;
	private final SignalDomain<R> domain;

	public TemporalMonitorUntil(TemporalMonitor<T, R> m1,
								TemporalMonitor<T, R> m2,
								SignalDomain<R> domain)
	{
		this(m1, null, m2, domain);
	}

	public TemporalMonitorUntil(TemporalMonitor<T, R> m1,
								Interval interval,
								TemporalMonitor<T, R> m2,
								SignalDomain<R> domain)
	{
		this.m1 = m1;
		this.interval = interval;
		this.m2 = m2;
		this.domain = domain;
	}

	@Override
	public Signal<R> monitor(Signal<T> signal) {
		Signal<R> s1 = m1.monitor(signal);
		Signal<R> s2 = m2.monitor(signal);
		return UntilOperator.computeUntil(domain, s1, interval, s2);
	}

}
