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

package eu.quanticol.moonlight.offline.monitoring.mfr;

import eu.quanticol.moonlight.core.formula.Formula;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.formula.AtomicFormula;
import eu.quanticol.moonlight.formula.Parameters;
import eu.quanticol.moonlight.formula.mfr.FilterFormula;
import eu.quanticol.moonlight.formula.mfr.MapFormula;
import eu.quanticol.moonlight.formula.mfr.ReduceFormula;
import eu.quanticol.moonlight.formula.mfr.SetFormula;
import eu.quanticol.moonlight.formula.temporal.SinceFormula;
import eu.quanticol.moonlight.offline.monitoring.spatialtemporal.SpatialTemporalMonitor;

import java.util.Map;
import java.util.function.Function;

/**
 * Alternative interface to perform (spatial) monitoring.
 * The key difference is that it is based on a visitor
 * design pattern over the formula tree which resorts
 * to {@code SpatialTemporalMonitor} methods for the implementation.
 * <p>
 * Note: Particularly useful in static environment.
 *
 * @param <S> Spatial Graph Edge Type
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 * @see SpatialTemporalMonitor
 */
public class MfrMonitoring<S, T, R> {
    private final Map<String, Function<Parameters, Function<T, R>>> atoms;
    private final Map<String, Function<SpatialModel<S>,
            DistanceStructure<S, ?>>> distanceFunctions;
    private final SignalDomain<R> domain;


    public MfrMonitoring(
            Map<String, Function<Parameters, Function<T, R>>> atomicPropositions,
            Map<String, Function<SpatialModel<S>,
                    DistanceStructure<S, ?>>> distanceFunctions,
            SignalDomain<R> domain) {
        super();
        this.atoms = atomicPropositions;
        this.domain = domain;
        this.distanceFunctions = distanceFunctions;
    }

    /**
     * Entry point of the monitoring program:
     * it launches the monitoring process over the formula f.
     *
     * @param f the formula to monitor
     * @return the result of the monitoring process.
     */
    public MfrMonitor<S, T, R> monitor(Formula f) {
        return switch (f) {
            // Classic operators
            case AtomicFormula atomic -> generateAtomicMonitor(atomic);
//            case NegationFormula negation -> generateNegationMonitor(negation);
//            case AndFormula and -> generateAndMonitor(and);
//            case OrFormula or -> generateOrMonitor(or);
            // Temporal Past Operators
//            case OnceFormula once -> generateOnceMonitor(once);
//            case HistoricallyFormula hs -> generateHistoricallyMonitor(hs);
            case SinceFormula since -> generateSinceMonitor(since);
            // Spatial Operators
//            case SomewhereFormula some -> generateSomewhereMonitor(some);
//            case EverywhereFormula every -> generateEverywhereMonitor(every);
//            case EscapeFormula escape -> generateEscapeMonitor(escape);
            case ReduceFormula reduce ->
                    generateReduceMonitor(reduce); // TODO: Java typed switch are still pretty dumb
            default -> illegalFormula(f);
        };
    }

    private <V> MfrSetMonitor<S, T, V> monitorSet(SetFormula f) {
        return switch (f) {
            case MapFormula map -> generateMapMonitor(map);
            case FilterFormula filter -> generateFilterFormula(filter);
            case Formula formula -> mapToLocationSet(formula, this::monitor);
            default -> throw new UnsupportedOperationException("illegal set " +
                    "formula");
        };
    }

    private <V> MfrSetMonitor<S, T, V> mapToLocationSet(
            Formula f,
            Function<Formula, MfrMonitor<S, T, R>> m) {
        return null;
    }

    private MfrMonitor<S, T, R> illegalFormula(Formula f) {
        throw new IllegalArgumentException("Unsupported formula: " + f);
    }

    private MfrMonitor<S, T, R> generateAtomicMonitor(AtomicFormula f) {
        var atomicFunc = atoms.get(f.getAtomicId());

        if (atomicFunc == null) {
            throw new IllegalArgumentException("Unknown atomic ID " +
                    f.getAtomicId());
        }
        Function<T, R> atomic = atomicFunc.apply(null);

        //return new MfrMonitorAtomic<>(atomic);
        return null; //TODO
    }

//    private MfrMonitor<S, T, R> generateOnceMonitor(OnceFormula f) {
//        var argMonitor = monitor(f.getArgument());
//
//        if (f.isUnbounded()) {
//            return onceMonitor(argMonitor, domain);
//        } else {
//            Interval interval = f.getInterval();
//            return onceMonitor(argMonitor, domain, interval);
//        }
//    }
//
//    private MfrMonitor<S, T, R> generateHistoricallyMonitor(
//            HistoricallyFormula f)
//    {
//        var argMonitor = monitor(f.getArgument());
//
//        if (f.isUnbounded()) {
//            return historicallyMonitor(argMonitor, domain);
//        } else {
//            Interval interval = f.getInterval();
//            return historicallyMonitor(argMonitor, domain, interval);
//        }
//    }

    private MfrMonitor<S, T, R> generateSinceMonitor(SinceFormula f) {
        var leftMonitor = monitor(f.getFirstArgument());
        var rightMonitor = monitor(f.getSecondArgument());
        var interval = f.getInterval();

        if (f.isUnbounded()) {
            return new MfrMonitorSince<>(leftMonitor, null,
                    rightMonitor, domain);
        } else {
            return new MfrMonitorSince<>(leftMonitor, interval,
                    rightMonitor, domain);
        }
    }

    private <V> MfrMonitor<S, T, R> generateReduceMonitor(ReduceFormula<V, R> f) {
        var aggregator = f.getAggregator();
        MfrSetMonitor<S, T, V> argMonitor = monitorSet(f.getArgument());
        var distanceFunction = distanceFunctions.get(f.getDistanceFunctionId());
        return new MfrMonitorReduce<>(argMonitor, aggregator, distanceFunction);
    }

    private <V> MfrSetMonitor<S, T, V> generateMapMonitor(MapFormula<V> f) {
        MfrSetMonitor<S, T, V> argMonitor = monitorSet(f.getArgument());
        return new MfrMonitorMap<>(f.getMapper(), argMonitor);
    }

    private <V> MfrSetMonitor<S, T, V> generateFilterFormula(FilterFormula<V> f) {
        MfrSetMonitor<S, T, V> argMonitor = monitorSet(f.getArgument());
        return new MfrMonitorFilter<>(f.getPredicate(), argMonitor);
    }
}