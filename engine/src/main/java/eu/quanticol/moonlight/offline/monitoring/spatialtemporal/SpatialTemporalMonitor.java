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

import java.util.function.Function;

import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.monitoring.temporal.*;
import eu.quanticol.moonlight.offline.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.core.formula.Interval;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;

/**
 * Primary SpatialTemporal Monitoring interface
 * It is based on a strategy design pattern, where each kind of operators
 * has a specific strategy implementation.
 *
 * Implementors must implement the
 * {@link #monitor(LocationService, SpatialTemporalSignal)} method, which is
 * called recursively until the atomic subformulae apply the given functions
 * on the signals.
 *
 * @param <S> Spatial Graph Edge Type
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see TemporalMonitor for the simpler (only temporal) one.
 *
 * For specific implementation of the different logic operators:
 * @see SpatialTemporalMonitorAtomic
 * @see SpatialTemporalMonitorUnaryOperator
 * @see SpatialTemporalMonitorBinaryOperator
 * @see SpatialTemporalMonitorFutureOperator
 * @see SpatialTemporalMonitorUntil
 * @see SpatialTemporalMonitorPastOperator
 * @see SpatialTemporalMonitorSince
 */
public interface SpatialTemporalMonitor<S, T, R> {
	
	SpatialTemporalSignal<R> monitor(LocationService<Double, S> locationService,
									 SpatialTemporalSignal<T> signal);
	
	static <S, T, R> SpatialTemporalMonitor<S, T, R> atomicMonitor(Function<T, R> atomic) {
		return new SpatialTemporalMonitorAtomic<>(atomic);
	}

	static <S, T, R> SpatialTemporalMonitor<S, T, R> andMonitor(
			SpatialTemporalMonitor<S, T, R> m1,
			SignalDomain<R> domain,
			SpatialTemporalMonitor<S, T, R> m2)
	{
		return new SpatialTemporalMonitorBinaryOperator<>(m1, domain::conjunction, m2);
	}
	
	static <S, T, R> SpatialTemporalMonitor<S, T, R> orMonitor(
			SpatialTemporalMonitor<S, T, R> m1,
			SignalDomain<R> domain,
			SpatialTemporalMonitor<S, T, R> m2)
	{
		return new SpatialTemporalMonitorBinaryOperator<>(m1, domain::disjunction, m2);
	}

	static <S, T, R> SpatialTemporalMonitor<S, T, R> impliesMonitor(
			SpatialTemporalMonitor<S, T, R> m1,
			SignalDomain<R> domain,
			SpatialTemporalMonitor<S, T, R> m2)
	{
		return new SpatialTemporalMonitorBinaryOperator<>(m1, domain::implies, m2);
	}
	static <S, T, R> SpatialTemporalMonitor<S, T, R> notMonitor(
			SpatialTemporalMonitor<S, T, R> m,
			SignalDomain<R> domain )
	{
		return new SpatialTemporalMonitorUnaryOperator<>(m, domain::negation);
	}

	static <S, T, R> SpatialTemporalMonitor<S, T, R> eventuallyMonitor(
			SpatialTemporalMonitor<S, T, R> m,
			Interval interval,
			SignalDomain<R> domain)
	{
		return new SpatialTemporalMonitorFutureOperator<>(m, interval, domain::disjunction, domain.min());
	}

	static <S, T, R> SpatialTemporalMonitor<S, T, R> eventuallyMonitor(
			SpatialTemporalMonitor<S, T, R> m,
			SignalDomain<R> domain)
	{
		return eventuallyMonitor(m, null, domain);
	}

	static <S, T, R> SpatialTemporalMonitor<S, T, R> globallyMonitor(
			SpatialTemporalMonitor<S, T, R> m,
			Interval interval,
			SignalDomain<R> domain)
	{
		return new SpatialTemporalMonitorFutureOperator<>(m, interval, domain::conjunction, domain.max());
	}

	static <S, T, R> SpatialTemporalMonitor<S, T, R> globallyMonitor(
			SpatialTemporalMonitor<S, T, R> m,
			SignalDomain<R> domain)
	{
		return globallyMonitor(m, null, domain);
	}
	
	static <S, T, R> SpatialTemporalMonitor<S, T, R> untilMonitor(
			SpatialTemporalMonitor<S, T, R> m1,
			Interval interval,
			SpatialTemporalMonitor<S, T, R> m2,
			SignalDomain<R> domain)
	{
		return new SpatialTemporalMonitorUntil<>(m1,interval,m2,domain);
	}
	
	static <S, T, R> SpatialTemporalMonitor<S, T, R> untilMonitor(
			SpatialTemporalMonitor<S, T, R> m1,
			SpatialTemporalMonitor<S, T, R> m2,
			SignalDomain<R> domain)
	{
		return untilMonitor(m1, null, m2, domain);
	}

	static <S, T, R> SpatialTemporalMonitor<S, T, R> sinceMonitor(
			SpatialTemporalMonitor<S, T, R> m1,
			Interval interval,
			SpatialTemporalMonitor<S, T, R> m2,
			SignalDomain<R> domain)
	{
		return new SpatialTemporalMonitorSince<>(m1,interval,m2,domain);
	}
	
	static <S, T, R> SpatialTemporalMonitor<S, T, R> sinceMonitor(
			SpatialTemporalMonitor<S, T, R> m1,
			SpatialTemporalMonitor<S, T, R> m2,
			SignalDomain<R> domain)
	{
		return sinceMonitor(m1, null, m2, domain);
	}
	
	static <S, T, R> SpatialTemporalMonitor<S, T, R> onceMonitor(
			SpatialTemporalMonitor<S, T, R> m,
			Interval interval,
			SignalDomain<R> domain)
	{
		return new SpatialTemporalMonitorPastOperator<>(m, interval, domain::disjunction, domain.min());
	}

	static <S, T, R> SpatialTemporalMonitor<S, T, R> onceMonitor(
			SpatialTemporalMonitor<S, T, R> m,
			SignalDomain<R> domain)
	{
		return onceMonitor(m,null,domain);
	}

	static <S, T, R> SpatialTemporalMonitor<S, T, R> historicallyMonitor(
			SpatialTemporalMonitor<S, T, R> m,
			Interval interval,
			SignalDomain<R> domain)
	{
		return new SpatialTemporalMonitorPastOperator<>(m, interval, domain::conjunction, domain.max());
	}

	static <S, T, R> SpatialTemporalMonitor<S, T, R> historicallyMonitor(
			SpatialTemporalMonitor<S, T, R> m,
			SignalDomain<R> domain)
	{
		return historicallyMonitor(m, null, domain);
	}
	
	static <S, T, R> SpatialTemporalMonitor<S, T, R> somewhereMonitor(
			SpatialTemporalMonitor<S, T, R> m ,
			Function<SpatialModel<S>,
					 DistanceStructure<S, ?>> distance,
			SignalDomain<R> domain)
	{
		return new SpatialTemporalMonitorSomewhere<>(m,distance,domain);
	}

	static <S, T, R> SpatialTemporalMonitor<S, T, R> everywhereMonitor(
			SpatialTemporalMonitor<S, T, R> m,
			Function<SpatialModel<S>,
					 DistanceStructure<S, ?>> distance,
			SignalDomain<R> domain)
	{
		return new SpatialTemporalMonitorEverywhere<>(m, distance, domain);
	}

	static <S, T, R> SpatialTemporalMonitor<S, T, R> escapeMonitor(
			SpatialTemporalMonitor<S, T, R> m,
			Function<SpatialModel<S>,
					 DistanceStructure<S, ?>> distance,
			SignalDomain<R> domain)
	{
		return new SpatialTemporalMonitorEscape<>(m, distance, domain);
	}

	static <S, T, R> SpatialTemporalMonitor<S, T, R> reachMonitor(
			SpatialTemporalMonitor<S, T, R> m1,
			Function<SpatialModel<S>,
					DistanceStructure<S, ?>> distance,
			SpatialTemporalMonitor<S, T, R> m2,
			SignalDomain<R> domain )
	{
		return new SpatialTemporalMonitorReach<>(m1,distance,m2,domain);
	}

	static <S, T, R> SpatialTemporalMonitor<S, T, R> surroundMonitor(
			SpatialTemporalMonitor<S, T, R> m,
			Interval interval,
			SignalDomain<R> domain)
	{
		return null;
	}

	static <S, T, R> SpatialTemporalMonitor<S, T, R> nextMonitor(
			SpatialTemporalMonitor<S, T, R> m,
			Interval interval,
			SignalDomain<T> domain)
	{
		return null;
	}

	static <S, T, R> SpatialTemporalMonitor<S, T, R> closureMonitor(
			SpatialTemporalMonitor<S, T, R> m,
			Interval interval,
			SignalDomain<T> domain)
	{
		return null;
	}


}
