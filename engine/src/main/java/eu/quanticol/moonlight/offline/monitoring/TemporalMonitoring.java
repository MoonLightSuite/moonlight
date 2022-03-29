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

package eu.quanticol.moonlight.offline.monitoring;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import eu.quanticol.moonlight.formula.classic.AndFormula;
import eu.quanticol.moonlight.formula.AtomicFormula;
import eu.quanticol.moonlight.formula.temporal.EventuallyFormula;
import eu.quanticol.moonlight.core.formula.Formula;
import eu.quanticol.moonlight.core.formula.FormulaVisitor;
import eu.quanticol.moonlight.formula.temporal.GloballyFormula;
import eu.quanticol.moonlight.formula.temporal.HistoricallyFormula;
import eu.quanticol.moonlight.core.formula.Interval;
import eu.quanticol.moonlight.formula.classic.NegationFormula;
import eu.quanticol.moonlight.formula.temporal.OnceFormula;
import eu.quanticol.moonlight.formula.classic.OrFormula;
import eu.quanticol.moonlight.formula.Parameters;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.formula.temporal.SinceFormula;
import eu.quanticol.moonlight.formula.temporal.UntilFormula;
import eu.quanticol.moonlight.offline.monitoring.temporal.TemporalMonitor;


import static eu.quanticol.moonlight.offline.monitoring.temporal.TemporalMonitor.*;

/**
 * Alternative interface to perform monitoring.
 * The key difference is that it is based on a visitor
 * design pattern over the formula tree which resorts
 * to TemporalMonitor methods for the implementation.
 *
 * Note: Particularly useful in static environment.
 *
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see FormulaVisitor
 * @see TemporalMonitor
 */
public class TemporalMonitoring<T, R> {
	private final Map<String, Function<Parameters, Function<T, R>>> atoms;
	private final SignalDomain<R> module;

	/**
	 * Initializes a monitoring process over the given interpretation domain.
	 * @param interpretation signal interpretation domain
	 */
	public TemporalMonitoring(SignalDomain<R> interpretation) {
		this(new HashMap<>(), interpretation);
	}

	/**
	 * Initializes a monitoring process over the given interpretation domain,
	 * and the given atomic propositions.
	 * @param atomicPropositions atomic propositions of interest
	 * @param interpretation signal interpretation domain
	 */
	public TemporalMonitoring(
		Map<String, Function<Parameters, Function<T, R>>> atomicPropositions,
		SignalDomain<R> interpretation)
	{
		this.atoms = atomicPropositions;
		this.module = interpretation;
	}

	/**
	 * Adds an atomic property to the monitored ones.
	 * @param name identifier of the atomic property
	 * @param atomicFunction the function that corresponds to the property
	 */
	public void addProperty(String name,
							Function<Parameters, Function<T, R>> atomicFunction)
	{
		atoms.put(name, atomicFunction);
	}

	/**
	 * Entry point of the monitoring program:
	 * it launches the monitoring process over the formula f.
	 *
	 * @param f the formula to monitor
	 * @return the result of the monitoring process.
	 */
	public TemporalMonitor<T, R> monitor(Formula f) {
		return switch(f) {
			// Classic operators
			case AtomicFormula atomic -> generateAtomicMonitor(atomic);
			case NegationFormula negation -> generateNegationMonitor(negation);
			case AndFormula and -> generateAndMonitor(and);
			case OrFormula or -> generateOrMonitor(or);
			// Temporal Future Operators
			case EventuallyFormula ev -> generateEventuallyMonitor(ev);
			case GloballyFormula globally -> generateGloballyMonitor(globally);
			case UntilFormula until -> generateUntilMonitor(until);
			// Temporal Past Operators
			case OnceFormula once -> generateOnceMonitor(once);
			case HistoricallyFormula hs -> generateHistoricallyMonitor(hs);
			case SinceFormula since -> generateSinceMonitor(since);
			default -> illegalFormula(f);
		};
	}

	private TemporalMonitor<T, R> illegalFormula(Formula f) {
		throw new IllegalArgumentException("Unsupported formula: " + f);
	}

	private TemporalMonitor<T, R> generateAtomicMonitor(AtomicFormula f) {
		var atomicFunc = atoms.get(f.getAtomicId());

		if (atomicFunc == null) {
			throw new IllegalArgumentException("Unknown atomic ID " +
					f.getAtomicId());
		}
		Function<T, R> atomic = atomicFunc.apply(null);

		return atomicMonitor(atomic);
	}

	private TemporalMonitor<T, R> generateAndMonitor(AndFormula f) {
		var leftMonitor = monitor(f.getFirstArgument());
		var rightMonitor = monitor(f.getSecondArgument());

		return andMonitor(leftMonitor, module , rightMonitor);
	}

	private TemporalMonitor<T, R> generateOrMonitor(OrFormula f) {
		var leftMonitor = monitor(f.getFirstArgument());
		var rightMonitor = monitor(f.getSecondArgument());

		return orMonitor(leftMonitor, module, rightMonitor);
	}

	private TemporalMonitor<T, R> generateNegationMonitor(NegationFormula f) {
		var argumentMonitoring = monitor(f.getArgument());
		return notMonitor(argumentMonitoring, module);
	}

	private TemporalMonitor<T, R> generateEventuallyMonitor(EventuallyFormula f)
	{
		var argMonitor = monitor(f.getArgument());

		if (f.isUnbounded()) {
			return eventuallyMonitor(argMonitor, module);
		} else {
			Interval interval = f.getInterval();
			return eventuallyMonitor(argMonitor, module, interval);
		}
	}

	private TemporalMonitor<T, R> generateGloballyMonitor(GloballyFormula f) {
		var argMonitor = monitor(f.getArgument());

		if (f.isUnbounded()) {
			return globallyMonitor(argMonitor, module);
		} else {
			Interval interval = f.getInterval();
			return globallyMonitor(argMonitor, module, interval);
		}
	}

	private TemporalMonitor<T, R> generateOnceMonitor(OnceFormula f) {
		var argMonitor = monitor(f.getArgument());

		if (f.isUnbounded()) {
			return onceMonitor(argMonitor, module);
		} else {
			Interval interval = f.getInterval();
			return onceMonitor(argMonitor, module, interval);
		}
	}

	private TemporalMonitor<T, R> generateHistoricallyMonitor(
			HistoricallyFormula f)
	{
		var argMonitor = monitor(f.getArgument());

		if (f.isUnbounded()) {
			return historicallyMonitor(argMonitor, module);
		} else {
			Interval interval = f.getInterval();
			return historicallyMonitor(argMonitor, module, interval);
		}
	}

	private TemporalMonitor<T, R> generateUntilMonitor(UntilFormula f) {
		var leftMonitor = monitor(f.getFirstArgument());
		var rightMonitor = monitor(f.getSecondArgument());

		if (f.isUnbounded()) {
			return untilMonitor(leftMonitor, rightMonitor, module);
		} else {
			return untilMonitor(leftMonitor, f.getInterval(),
								rightMonitor, module);
		}
	}

	private TemporalMonitor<T, R> generateSinceMonitor(SinceFormula f) {
		var leftMonitor = monitor(f.getFirstArgument());
		var rightMonitor = monitor(f.getSecondArgument());

		if (f.isUnbounded()) {
			return sinceMonitor(leftMonitor, rightMonitor, module);
		} else {
			return sinceMonitor(leftMonitor, f.getInterval(),
								rightMonitor, module);
		}
	}
}
