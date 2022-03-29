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

import eu.quanticol.moonlight.core.formula.Formula;
import eu.quanticol.moonlight.core.formula.FormulaVisitor;
import eu.quanticol.moonlight.core.formula.Interval;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.formula.classic.AndFormula;
import eu.quanticol.moonlight.formula.classic.NegationFormula;
import eu.quanticol.moonlight.formula.classic.OrFormula;
import eu.quanticol.moonlight.formula.spatial.EscapeFormula;
import eu.quanticol.moonlight.formula.spatial.EverywhereFormula;
import eu.quanticol.moonlight.formula.spatial.ReachFormula;
import eu.quanticol.moonlight.formula.spatial.SomewhereFormula;
import eu.quanticol.moonlight.formula.temporal.*;
import eu.quanticol.moonlight.offline.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.core.signal.SignalDomain;

import java.util.Map;
import java.util.function.Function;

import static eu.quanticol.moonlight.offline.monitoring.spatialtemporal.SpatialTemporalMonitor.*;

/**
 * Alternative interface to perform (spatial) monitoring.
 * The key difference is that it is based on a visitor
 * design pattern over the formula tree which resorts
 * to {@code SpatialTemporalMonitor} methods for the implementation.
 *
 * Note: Particularly useful in static environment.
 *
 * @param <S> Spatial Graph Edge Type
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see SpatialTemporalMonitor
 */
public class SpatialTemporalMonitoring<S, T, R> {
    private final Map<String, Function<Parameters, Function<T, R>>> atoms;
    private final Map<String, Function<SpatialModel<S>, DistanceStructure<S, ?>>> distanceFunctions;
    private final SignalDomain<R> module;


    public SpatialTemporalMonitoring(
            Map<String, Function<Parameters, Function<T, R>>> atomicPropositions,
            Map<String, Function<SpatialModel<S>,
                    DistanceStructure<S, ?>>> distanceFunctions,
            SignalDomain<R> module)
    {
        super();
        this.atoms = atomicPropositions;
        this.module = module;
        this.distanceFunctions = distanceFunctions;
    }

    /**
     * Entry point of the monitoring program:
     * it launches the monitoring process over the formula f.
     *
     * @param f the formula to monitor
     * @return the result of the monitoring process.
     */
    public SpatialTemporalMonitor<S, T, R> monitor(Formula f) {
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
            // Spatial Operators
            case SomewhereFormula some -> generateSomewhereMonitor(some);
            case EverywhereFormula every -> generateEverywhereMonitor(every);
            case EscapeFormula escape -> generateEscapeMonitor(escape);
            case ReachFormula reach -> generateReachMonitor(reach);
            default -> illegalFormula(f);
        };
    }

    private SpatialTemporalMonitor<S, T, R> illegalFormula(Formula f) {
        throw new IllegalArgumentException("Unsupported formula: " + f);
    }

    private SpatialTemporalMonitor<S, T, R> generateAtomicMonitor(AtomicFormula f) {
        var atomicFunc = atoms.get(f.getAtomicId());

        if (atomicFunc == null) {
            throw new IllegalArgumentException("Unknown atomic ID " +
                    f.getAtomicId());
        }
        Function<T, R> atomic = atomicFunc.apply(null);

        return atomicMonitor(atomic);
    }

    private SpatialTemporalMonitor<S, T, R> generateAndMonitor(AndFormula f) {
        var leftMonitor = monitor(f.getFirstArgument());
        var rightMonitor = monitor(f.getSecondArgument());

        return andMonitor(leftMonitor, module , rightMonitor);
    }

    private SpatialTemporalMonitor<S, T, R> generateOrMonitor(OrFormula f) {
        var leftMonitor = monitor(f.getFirstArgument());
        var rightMonitor = monitor(f.getSecondArgument());

        return orMonitor(leftMonitor, module, rightMonitor);
    }

    private SpatialTemporalMonitor<S, T, R> generateNegationMonitor(NegationFormula f) {
        var argumentMonitoring = monitor(f.getArgument());
        return notMonitor(argumentMonitoring, module);
    }

    private SpatialTemporalMonitor<S, T, R> generateEventuallyMonitor(EventuallyFormula f)
    {
        var argMonitor = monitor(f.getArgument());

        if (f.isUnbounded()) {
            return eventuallyMonitor(argMonitor, module);
        } else {
            Interval interval = f.getInterval();
            return eventuallyMonitor(argMonitor, module, interval);
        }
    }

    private SpatialTemporalMonitor<S, T, R> generateGloballyMonitor(GloballyFormula f) {
        var argMonitor = monitor(f.getArgument());

        if (f.isUnbounded()) {
            return globallyMonitor(argMonitor, module);
        } else {
            Interval interval = f.getInterval();
            return globallyMonitor(argMonitor, module, interval);
        }
    }

    private SpatialTemporalMonitor<S, T, R> generateOnceMonitor(OnceFormula f) {
        var argMonitor = monitor(f.getArgument());

        if (f.isUnbounded()) {
            return onceMonitor(argMonitor, module);
        } else {
            Interval interval = f.getInterval();
            return onceMonitor(argMonitor, module, interval);
        }
    }

    private SpatialTemporalMonitor<S, T, R> generateHistoricallyMonitor(
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

    private SpatialTemporalMonitor<S, T, R> generateUntilMonitor(UntilFormula f) {
        var leftMonitor = monitor(f.getFirstArgument());
        var rightMonitor = monitor(f.getSecondArgument());

        if (f.isUnbounded()) {
            return untilMonitor(leftMonitor, rightMonitor, module);
        } else {
            return untilMonitor(leftMonitor, f.getInterval(),
                    rightMonitor, module);
        }
    }

    private SpatialTemporalMonitor<S, T, R> generateSinceMonitor(SinceFormula f) {
        var leftMonitor = monitor(f.getFirstArgument());
        var rightMonitor = monitor(f.getSecondArgument());

        if (f.isUnbounded()) {
            return sinceMonitor(leftMonitor, rightMonitor, module);
        } else {
            return sinceMonitor(leftMonitor, f.getInterval(),
                    rightMonitor, module);
        }
    }

    private SpatialTemporalMonitor<S, T, R> generateReachMonitor(ReachFormula f) {
        var leftMonitor = monitor(f.getFirstArgument());
        var rightMonitor = monitor(f.getSecondArgument());

        var distanceFunction= distanceFunctions.get(f.getDistanceFunctionId());
        return reachMonitor(leftMonitor, distanceFunction,
                            rightMonitor, module);
    }

    private SpatialTemporalMonitor<S, T, R> generateSomewhereMonitor(SomewhereFormula f) {
        var argMonitor = monitor(f.getArgument());

        var distanceFunction= distanceFunctions.get(f.getDistanceFunctionId());
        return somewhereMonitor(argMonitor, distanceFunction, module);
    }

    private SpatialTemporalMonitor<S, T, R> generateEverywhereMonitor(EverywhereFormula f) {
        var argMonitor = monitor(f.getArgument());

        var distanceFunction= distanceFunctions.get(f.getDistanceFunctionId());
        return everywhereMonitor(argMonitor, distanceFunction, module);
    }

    private SpatialTemporalMonitor<S, T, R> generateEscapeMonitor(EscapeFormula f) {
        var argMonitor = monitor(f.getArgument());

        var distanceFunction= distanceFunctions.get(f.getDistanceFunctionId());
        return escapeMonitor(argMonitor, distanceFunction, module);
    }
}
