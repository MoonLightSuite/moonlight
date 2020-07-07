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

import java.util.function.Function;

import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.formula.SignalDomain;
import eu.quanticol.moonlight.signal.Signal;

/**
 * Primary Monitoring interface
 * TODO: there is no reason for this to be an abstract class -> change to interface!
 * It is based on a strategy design pattern, where each kind of operators
 * has a specific strategy implementation.
 *
 * Implementors must implement the {@link #monitor(Signal)} method, which is
 * called recursively until the atomic subformulae apply the given functions
 * on the signals.
 *
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * For specific implementation of the different logic operators:
 * @see TemporalMonitorAtomic
 * @see TemporalMonitorUnary
 * @see TemporalMonitorBinary
 * @see TemporalMonitorFutureOperator
 * @see TemporalMonitorUntil
 * @see TemporalMonitorPastOperator
 * @see TemporalMonitorSince
 */
public abstract class TemporalMonitor<T, R> {

	public abstract Signal<R> monitor(Signal<T> signal);

	public static <T, R> TemporalMonitor<T, R> atomicMonitor(Function<T, R> atomic) {
		return new TemporalMonitorAtomic<>(atomic);
	}

	public static <T, R> TemporalMonitor<T, R> andMonitor(TemporalMonitor<T, R> m1, SignalDomain<R> domain, TemporalMonitor<T, R> m2) {
		return new TemporalMonitorBinary<>(m1, domain::conjunction, m2);
	}

	public static <T, R> TemporalMonitor<T, R> orMonitor(TemporalMonitor<T, R> m1, SignalDomain<R> domain, TemporalMonitor<T, R> m2) {
		return new TemporalMonitorBinary<>(m1, domain::disjunction, m2);
	}

	public static <T, R> TemporalMonitor<T, R> impliesMonitor(TemporalMonitor<T, R> m1, SignalDomain<R> domain, TemporalMonitor<T, R> m2) {
		return new TemporalMonitorBinary<>(m1, domain::implies, m2);
	}

	public static <T, R> TemporalMonitor<T, R> notMonitor(TemporalMonitor<T, R> m, SignalDomain<R> domain) {
		return new TemporalMonitorUnary<>(m, domain::negation);
	}

	public static <T, R> TemporalMonitor<T, R> eventuallyMonitor(TemporalMonitor<T, R> m, SignalDomain<R> domain) {
		return new TemporalMonitorFutureOperator<>(m, domain::disjunction, domain.min());
	}

	public static <T, R> TemporalMonitor<T, R> eventuallyMonitor(TemporalMonitor<T, R> m, SignalDomain<R> domain, Interval interval) {
		return new TemporalMonitorFutureOperator<>(m, domain::disjunction, domain.min(), interval);
	}

	public static <T, R> TemporalMonitor<T, R> globallyMonitor(TemporalMonitor<T, R> m, SignalDomain<R> domain) {
		return new TemporalMonitorFutureOperator<>(m, domain::conjunction, domain.max());
	}

	public static <T, R> TemporalMonitor<T, R> globallyMonitor(TemporalMonitor<T, R> m, SignalDomain<R> domain, Interval interval) {
		return new TemporalMonitorFutureOperator<>(m, domain::conjunction, domain.max(), interval);
	}

	public static <T, R> TemporalMonitor<T, R> untilMonitor(TemporalMonitor<T, R> m1, TemporalMonitor<T, R> m2, SignalDomain<R> domain) {
		return new TemporalMonitorUntil<>(m1, m2, domain);
	}

	public static <T, R> TemporalMonitor<T, R> untilMonitor(TemporalMonitor<T, R> m1, Interval interval, TemporalMonitor<T, R> m2, SignalDomain<R> domain) {
		return new TemporalMonitorUntil<>(m1, interval, m2, domain);
	}

	public static <T, R> TemporalMonitor<T, R> historicallyMonitor(TemporalMonitor<T, R> m, SignalDomain<R> domain) {
		return new TemporalMonitorPastOperator<>(m, domain::conjunction, domain.max());
	}

	public static <T, R> TemporalMonitor<T, R> historicallyMonitor(TemporalMonitor<T, R> m, SignalDomain<R> domain, Interval interval) {
		return new TemporalMonitorPastOperator<>(m, domain::conjunction, domain.max(), interval);
	}

	public static <T, R> TemporalMonitor<T, R> onceMonitor(TemporalMonitor<T, R> m, SignalDomain<R> domain) {
		return new TemporalMonitorPastOperator<>(m, domain::disjunction, domain.min());
	}

	public static <T, R> TemporalMonitor<T, R> onceMonitor(TemporalMonitor<T, R> m, SignalDomain<R> domain, Interval interval) {
		return new TemporalMonitorPastOperator<>(m, domain::disjunction, domain.min(), interval);
	}

	public static <T, R> TemporalMonitor<T, R> sinceMonitor(TemporalMonitor<T, R> m1, TemporalMonitor<T, R> m2, SignalDomain<R> domain) {
		return new TemporalMonitorSince<>(m1, m2, domain);
	}

	public static <T, R> TemporalMonitor<T, R> sinceMonitor(TemporalMonitor<T, R> m1, Interval interval, TemporalMonitor<T, R> m2, SignalDomain<R> domain) {
		return new TemporalMonitorSince<>(m1, interval, m2, domain);
	}
}