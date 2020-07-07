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

package eu.quanticol.moonlight.monitoring.temporal;

import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.formula.SignalDomain;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SignalCursor;

import static eu.quanticol.moonlight.monitoring.temporal.TemporalMonitorPastOperator.computeSignal;

/**
 * Strategy to interpret the Since operator
 *
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see TemporalMonitor
 */
public class TemporalMonitorSince<T, R> extends TemporalMonitor<T, R> {

	private final TemporalMonitor<T, R> m1;
	private final Interval interval;
	private final TemporalMonitor<T, R> m2;
	private final SignalDomain<R> domain;

	public TemporalMonitorSince(TemporalMonitor<T, R> m1,
								TemporalMonitor<T, R> m2,
								SignalDomain<R> domain)
	{
		this(m1, null, m2, domain);
	}

	public TemporalMonitorSince(TemporalMonitor<T, R> m1,
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
		return computeSince(domain, m1.monitor(signal),
							interval, m2.monitor(signal));
	}

	//TODO: this should be at most protected
	public static <T> Signal<T> computeSince(SignalDomain<T> domain,
											 Signal<T> s1, Interval interval,
											 Signal<T> s2)
	{
		Signal<T> unboundedMonitoring = computeSince(domain,s1,s2);
		if (interval == null) {
			return unboundedMonitoring;
		}
		Signal<T> onceMonitoring = computeSignal(s2, interval,
											domain::disjunction, domain.max());

		return Signal.apply(unboundedMonitoring, domain::conjunction,
																onceMonitoring);
	}

	//TODO: this should be at most protected
	public static <T> Signal<T> computeSince(SignalDomain<T> domain,
											 Signal<T> s1,
											 Signal<T> s2) {
		Signal<T> result = new Signal<>();
		SignalCursor<T> c1 = s1.getIterator(true);
		SignalCursor<T> c2 = s2.getIterator(true);
		double start = Math.max( c1.time() , c2.time() );
		double end = Math.min(s1.end(), s2.end());
		double time = start;
		T current = domain.min();
		c1.move(time);
		c2.move(time);
		while (!c1.completed()&&!c2.completed()) {
			result.add(time, domain.disjunction(c2.value(),
									domain.conjunction(c1.value(), current)));
			time = Math.min(c1.nextTime(), c2.nextTime());
			c1.move(time);
			c2.move(time);
		} 
		result.endAt(end);
		return result;
	}

}
