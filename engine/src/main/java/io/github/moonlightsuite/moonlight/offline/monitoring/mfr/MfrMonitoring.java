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

package io.github.moonlightsuite.moonlight.offline.monitoring.mfr;

import io.github.moonlightsuite.moonlight.core.formula.Formula;
import io.github.moonlightsuite.moonlight.core.signal.SignalDomain;
import io.github.moonlightsuite.moonlight.core.space.DistanceStructure;
import io.github.moonlightsuite.moonlight.core.space.LocationService;
import io.github.moonlightsuite.moonlight.core.space.SpatialModel;
import io.github.moonlightsuite.moonlight.formula.AtomicFormula;
import io.github.moonlightsuite.moonlight.formula.Parameters;
import io.github.moonlightsuite.moonlight.formula.mfr.*;
import io.github.moonlightsuite.moonlight.formula.temporal.SinceFormula;
import io.github.moonlightsuite.moonlight.offline.monitoring.spatialtemporal.SpatialTemporalMonitor;

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
    private final LocationService<Double, S> locationService;
    private final SignalDomain<R> domain;


    //TODO: add locationService
    public MfrMonitoring(
            Map<String, Function<Parameters, Function<T, R>>> atomicPropositions,
            Map<String, Function<SpatialModel<S>,
                    DistanceStructure<S, ?>>> distanceFunctions,
            SignalDomain<R> domain,
            LocationService<Double, S> locationService) {
        super();
        this.atoms = atomicPropositions;
        this.distanceFunctions = distanceFunctions;
        this.domain = domain;
        this.locationService = locationService;
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
            case BinaryFormula binary -> generateBinaryMonitor(binary);
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

    private <V> MfrSetMonitor<S, T, R> monitorSet(SetFormula f) {
        return switch (f) {
            case MapFormula map -> generateMapMonitor(map);
            case FilterFormula filter -> generateFilterFormula(filter);
            case Formula formula -> mapToLocationSet(formula, this::monitor);
            default -> throw new UnsupportedOperationException("illegal set " +
                    "formula");
        };
    }

    private <V> MfrSetMonitor<S, T, R> mapToLocationSet(
            Formula f,
            Function<Formula, MfrMonitor<S, T, R>> m) {
        return monitor(f);
    }

    private <K> MfrMonitor<S, T, K> illegalFormula(Formula f) {
        throw new IllegalArgumentException("Unsupported formula: " + f);
    }

    private <K> MfrMonitor<S, T, R> generateAtomicMonitor(AtomicFormula f) {
        Function<Parameters, Function<T, R>> atomicFunc =
                atoms.get(f.getAtomicId());

        if (atomicFunc == null) {
            throw new IllegalArgumentException("Unknown atomic ID " +
                    f.getAtomicId());
        }
        //TODO: weird 'Parameters' object, to be removed
        Function<T, R> atomic = atomicFunc.apply(null);

        return new MfrMonitorAtomic<>(atomic);
    }

    private <K> MfrMonitor<S, T, R> generateBinaryMonitor(BinaryFormula<R> f) {
        var operator = f.getOperator();
        MfrMonitor<S, T, R> leftArg = monitor(f.getLeftArgument());
        MfrMonitor<S, T, R> rightArg = monitor(f.getRightArgument());

        return new MfrMonitorBinary<>(operator, leftArg, rightArg);
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

    private <K> MfrMonitor<S, T, R> generateSinceMonitor(SinceFormula f) {
        MfrMonitor<S, T, R> leftArg = monitor(f.getFirstArgument());
        MfrMonitor<S, T, R> rightArg = monitor(f.getSecondArgument());
        var interval = f.isUnbounded() ? null : f.getInterval();
        return new MfrMonitorSince<>(leftArg, interval, rightArg, domain);
    }

    private <V> MfrMonitor<S, T, R> generateReduceMonitor(ReduceFormula<R, R> f) {
        var aggregator = f.getAggregator();
        MfrSetMonitor<S, T, R> argMonitor = monitorSet(f.getArgument());
        var distanceFunction = distanceFunctions.get(f.getDistanceFunctionId());
        return new MfrMonitorReduce<>(argMonitor, aggregator,
                distanceFunction, locationService);
    }

    private <V> MfrSetMonitor<S, T, R> generateMapMonitor(MapFormula<R> f) {
        MfrSetMonitor<S, T, R> argMonitor = monitorSet(f.getArgument());
        return new MfrMonitorMap<>(f.getMapper(), argMonitor);
    }

    private <V> MfrSetMonitor<S, T, R> generateFilterFormula(FilterFormula<R> f) {
        MfrSetMonitor<S, T, R> argMonitor = monitorSet(f.getArgument());
        return new MfrMonitorFilter<>(f.getPredicate(), argMonitor);
    }
}
