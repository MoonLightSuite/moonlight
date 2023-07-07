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

package io.github.moonlightsuite.moonlight.offline.monitoring;

import io.github.moonlightsuite.moonlight.core.formula.Formula;
import io.github.moonlightsuite.moonlight.core.formula.Interval;
import io.github.moonlightsuite.moonlight.core.signal.SignalDomain;
import io.github.moonlightsuite.moonlight.core.space.DistanceStructure;
import io.github.moonlightsuite.moonlight.core.space.SpatialModel;
import io.github.moonlightsuite.moonlight.formula.AtomicFormula;
import io.github.moonlightsuite.moonlight.formula.Parameters;
import io.github.moonlightsuite.moonlight.formula.classic.AndFormula;
import io.github.moonlightsuite.moonlight.formula.classic.NegationFormula;
import io.github.moonlightsuite.moonlight.formula.classic.OrFormula;
import io.github.moonlightsuite.moonlight.formula.spatial.EscapeFormula;
import io.github.moonlightsuite.moonlight.formula.spatial.EverywhereFormula;
import io.github.moonlightsuite.moonlight.formula.spatial.ReachFormula;
import io.github.moonlightsuite.moonlight.formula.spatial.SomewhereFormula;
import io.github.moonlightsuite.moonlight.formula.temporal.*;
import io.github.moonlightsuite.moonlight.offline.monitoring.spatialtemporal.SpatialTemporalMonitor;

import java.util.Map;
import java.util.function.Function;

import static io.github.moonlightsuite.moonlight.offline.monitoring.spatialtemporal.SpatialTemporalMonitor.*;

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
public class SpatialTemporalMonitoring<S, T, R> {
    private final Map<String, Function<Parameters, Function<T, R>>> atoms;
    private final Map<String, Function<SpatialModel<S>, DistanceStructure<S, ?>>> distanceFunctions;
    private final SignalDomain<R> module;
    private final boolean parallel;


    public SpatialTemporalMonitoring(
            Map<String, Function<Parameters, Function<T, R>>> atomicPropositions,
            Map<String, Function<SpatialModel<S>,
                    DistanceStructure<S, ?>>> distanceFunctions,
            SignalDomain<R> module, boolean parallelize) {
        super();
        this.atoms = atomicPropositions;
        this.module = module;
        this.distanceFunctions = distanceFunctions;
        this.parallel = parallelize;
    }

    public SpatialTemporalMonitoring(
            Map<String, Function<Parameters, Function<T, R>>> atomicPropositions,
            Map<String, Function<SpatialModel<S>,
                    DistanceStructure<S, ?>>> distanceFunctions,
            SignalDomain<R> module) {
        this(atomicPropositions, distanceFunctions, module, false);
    }

    private SpatialTemporalMonitor<S, T, R> generateMonitor(AtomicFormula f) {
        var atomicFunc = atoms.get(f.getAtomicId());

        if (atomicFunc == null) {
            throw new IllegalArgumentException("Unknown atomic ID " +
                    f.getAtomicId());
        }
        Function<T, R> atomic = atomicFunc.apply(null);

        return atomicMonitor(atomic);
    }

    private SpatialTemporalMonitor<S, T, R> generateMonitor(AndFormula f) {
        var leftMonitor = monitor(f.getFirstArgument());
        var rightMonitor = monitor(f.getSecondArgument());

        return andMonitor(leftMonitor, module, rightMonitor);
    }

    /**
     * Entry point of the monitoring program:
     * it launches the monitoring process over the formula f.
     *
     * @param f the formula to monitor
     * @return the result of the monitoring process.
     */
    public <F extends Formula> SpatialTemporalMonitor<S, T, R> monitor(F f) {
        return switch (f) {
            // Classic operators
            case AtomicFormula atomic -> generateMonitor(atomic);
            case NegationFormula negation -> generateMonitor(negation);
            case AndFormula and -> generateMonitor(and);
            case OrFormula or -> generateMonitor(or);
            // Temporal Future Operators
            case EventuallyFormula ev -> generateMonitor(ev);
            case GloballyFormula globally -> generateMonitor(globally);
            case UntilFormula until -> generateMonitor(until);
            // Temporal Past Operators
            case OnceFormula once -> generateMonitor(once);
            case HistoricallyFormula hs -> generateMonitor(hs);
            case SinceFormula since -> generateMonitor(since);
            // Spatial Operators
            case SomewhereFormula some -> generateMonitor(some);
            case EverywhereFormula every -> generateMonitor(every);
            case EscapeFormula escape -> generateMonitor(escape);
            case ReachFormula reach -> generateMonitor(reach);
            default -> illegalFormula(f);
        };
    }

    private SpatialTemporalMonitor<S, T, R> illegalFormula(Formula f) {
        throw new IllegalArgumentException("Unsupported formula: " + f);
    }

    private SpatialTemporalMonitor<S, T, R> generateMonitor(OrFormula f) {
        var leftMonitor = monitor(f.getFirstArgument());
        var rightMonitor = monitor(f.getSecondArgument());

        return orMonitor(leftMonitor, module, rightMonitor);
    }

    private SpatialTemporalMonitor<S, T, R> generateMonitor(NegationFormula f) {
        var argumentMonitoring = monitor(f.getArgument());
        return notMonitor(argumentMonitoring, module);
    }

    private SpatialTemporalMonitor<S, T, R> generateMonitor(EventuallyFormula f) {
        var argMonitor = monitor(f.getArgument());

        if (f.isUnbounded()) {
            return eventuallyMonitor(argMonitor, module);
        } else {
            Interval interval = f.getInterval();
            return eventuallyMonitor(argMonitor, module, interval);
        }
    }

    private SpatialTemporalMonitor<S, T, R> generateMonitor(GloballyFormula f) {
        var argMonitor = monitor(f.getArgument());

        if (f.isUnbounded()) {
            return globallyMonitor(argMonitor, module);
        } else {
            Interval interval = f.getInterval();
            return globallyMonitor(argMonitor, module, interval);
        }
    }

    private SpatialTemporalMonitor<S, T, R> generateMonitor(OnceFormula f) {
        var argMonitor = monitor(f.getArgument());

        if (f.isUnbounded()) {
            return onceMonitor(argMonitor, module);
        } else {
            Interval interval = f.getInterval();
            return onceMonitor(argMonitor, module, interval);
        }
    }

    private SpatialTemporalMonitor<S, T, R> generateMonitor(
            HistoricallyFormula f) {
        var argMonitor = monitor(f.getArgument());

        if (f.isUnbounded()) {
            return historicallyMonitor(argMonitor, module);
        } else {
            Interval interval = f.getInterval();
            return historicallyMonitor(argMonitor, module, interval);
        }
    }

    private SpatialTemporalMonitor<S, T, R> generateMonitor(UntilFormula f) {
        var leftMonitor = monitor(f.getFirstArgument());
        var rightMonitor = monitor(f.getSecondArgument());

        if (f.isUnbounded()) {
            return untilMonitor(leftMonitor, rightMonitor, module);
        } else {
            return untilMonitor(leftMonitor, f.getInterval(),
                    rightMonitor, module);
        }
    }

    private SpatialTemporalMonitor<S, T, R> generateMonitor(SinceFormula f) {
        var leftMonitor = monitor(f.getFirstArgument());
        var rightMonitor = monitor(f.getSecondArgument());

        if (f.isUnbounded()) {
            return sinceMonitor(leftMonitor, rightMonitor, module);
        } else {
            return sinceMonitor(leftMonitor, f.getInterval(),
                    rightMonitor, module);
        }
    }

    private SpatialTemporalMonitor<S, T, R> generateMonitor(ReachFormula f) {
        var leftMonitor = monitor(f.getFirstArgument());
        var rightMonitor = monitor(f.getSecondArgument());

        var distanceFunction = distanceFunctions.get(f.getDistanceFunctionId());
        return reachMonitor(leftMonitor, distanceFunction,
                rightMonitor, module);
    }

    private SpatialTemporalMonitor<S, T, R> generateMonitor(SomewhereFormula f) {
        var argMonitor = monitor(f.getArgument());

        var distanceFunction = distanceFunctions.get(f.getDistanceFunctionId());
        return somewhereMonitor(argMonitor, distanceFunction, module, parallel);
    }

    private SpatialTemporalMonitor<S, T, R> generateMonitor(EverywhereFormula f) {
        var argMonitor = monitor(f.getArgument());

        var distanceFunction = distanceFunctions.get(f.getDistanceFunctionId());
        return everywhereMonitor(argMonitor, distanceFunction, module,
                parallel);
    }

    private SpatialTemporalMonitor<S, T, R> generateMonitor(EscapeFormula f) {
        var argMonitor = monitor(f.getArgument());

        var distanceFunction = distanceFunctions.get(f.getDistanceFunctionId());
        return escapeMonitor(argMonitor, distanceFunction, module);
    }
}
