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
public class TemporalMonitoring<T, R> implements
		FormulaVisitor<Parameters, TemporalMonitor<T, R>>
{
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
	 * @param params monitoring optional parameters
	 * @return the result of the monitoring process.
	 */
	public TemporalMonitor<T, R> monitor(Formula f, Parameters params) {
		return f.accept(this, params);
	}

	@Override
	public TemporalMonitor<T, R> visit(AtomicFormula atomicFormula,
									   Parameters parameters)
	{
		Function<Parameters, Function<T, R>> f = atoms.get(atomicFormula.getAtomicId());

		if (f == null) {
			throw new IllegalArgumentException("Unknown atomic ID " +
												atomicFormula.getAtomicId());
		}
		Function<T, R> atomic = f.apply(parameters);

		return TemporalMonitor.atomicMonitor(atomic);
	}

	@Override
	public TemporalMonitor<T, R> visit(AndFormula andFormula,
									   Parameters parameters)
	{
		TemporalMonitor<T, R> leftMonitoring = andFormula
											  .getFirstArgument()
											  .accept(this, parameters);
		TemporalMonitor<T, R> rightMonitoring = andFormula
											   .getSecondArgument()
											   .accept(this, parameters);

		return TemporalMonitor.andMonitor(leftMonitoring, module , rightMonitoring);
	}

	@Override
	public TemporalMonitor<T, R> visit(NegationFormula negationFormula,
									   Parameters parameters)
	{
		TemporalMonitor<T, R> argumentMonitoring = negationFormula.getArgument().accept(this, parameters);

		return TemporalMonitor.notMonitor(argumentMonitoring, module);
	}

	@Override
	public TemporalMonitor<T, R> visit(OrFormula orFormula,
									   Parameters parameters)
	{
		TemporalMonitor<T, R> leftMonitoring = orFormula
											  .getFirstArgument()
											  .accept(this, parameters);
		TemporalMonitor<T, R> rightMonitoring = orFormula
											   .getSecondArgument()
											   .accept(this, parameters);

		return TemporalMonitor.orMonitor(leftMonitoring, module , rightMonitoring);
	}

	@Override
	public TemporalMonitor<T, R> visit(EventuallyFormula eventuallyFormula,
									   Parameters parameters)
	{
		TemporalMonitor<T, R> monitoringArg = eventuallyFormula
											 .getArgument()
											 .accept(this, parameters);

		if (eventuallyFormula.isUnbounded()) {
			return TemporalMonitor.eventuallyMonitor(monitoringArg, module);
		} else {
			Interval interval = eventuallyFormula.getInterval();
			return TemporalMonitor.eventuallyMonitor(monitoringArg, module, interval);
		}
	}

	@Override
	public TemporalMonitor<T, R> visit(GloballyFormula globallyFormula,
									   Parameters parameters)
	{
		TemporalMonitor<T, R> monitoringArg = globallyFormula
											 .getArgument()
											 .accept(this, parameters);

		if (globallyFormula.isUnbounded()) {
			return TemporalMonitor.globallyMonitor(monitoringArg, module);
		} else {
			Interval interval = globallyFormula.getInterval();
			return TemporalMonitor.globallyMonitor(monitoringArg, module, interval);
		}
	}

	@Override
	public TemporalMonitor<T, R> visit(UntilFormula untilFormula,
									   Parameters parameters)
	{
		TemporalMonitor<T, R> firstMonitoring = untilFormula
											   .getFirstArgument()
											   .accept(this, parameters);
		TemporalMonitor<T, R> secondMonitoring = untilFormula
												.getSecondArgument()
												.accept(this, parameters);

		if (untilFormula.isUnbounded()) {
			return TemporalMonitor.untilMonitor(firstMonitoring, secondMonitoring, module);
		} else {
			return TemporalMonitor.untilMonitor(firstMonitoring, untilFormula.getInterval(),
								secondMonitoring, module);
		}
	}

	@Override
	public TemporalMonitor<T, R> visit(SinceFormula sinceFormula,
									   Parameters parameters)
	{
		TemporalMonitor<T, R> firstMonitoring = sinceFormula
											   .getFirstArgument()
											   .accept(this, parameters);
		TemporalMonitor<T, R> secondMonitoring = sinceFormula
												.getSecondArgument()
												.accept(this, parameters);

		if (sinceFormula.isUnbounded()) {
			return TemporalMonitor.sinceMonitor(firstMonitoring, secondMonitoring, module);
		} else {
			return TemporalMonitor.sinceMonitor(firstMonitoring, sinceFormula.getInterval(),
								secondMonitoring, module);
		}
	}

	@Override
	public TemporalMonitor<T, R> visit(HistoricallyFormula historicallyFormula,
									   Parameters parameters)
	{
		TemporalMonitor<T, R> monitoringArg = historicallyFormula
											 .getArgument()
											 .accept(this, parameters);

		if (historicallyFormula.isUnbounded()) {
			return TemporalMonitor.historicallyMonitor(monitoringArg, module);
		} else {
			return TemporalMonitor.historicallyMonitor(monitoringArg, module,
									   historicallyFormula.getInterval());
		}
	}

	@Override
	public TemporalMonitor<T, R> visit(OnceFormula onceFormula,
									   Parameters parameters)
	{
		TemporalMonitor<T, R> monitoringArg = onceFormula
										     .getArgument()
											 .accept(this, parameters);

		if (onceFormula.isUnbounded()) {
			return TemporalMonitor.onceMonitor(monitoringArg, module);
		} else {
			Interval interval = onceFormula.getInterval();
			return TemporalMonitor.onceMonitor(monitoringArg, module, interval);
		}
	}

}