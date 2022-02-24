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

package eu.quanticol.moonlight.monitoring;

import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.core.space.DefaultDistanceStructure;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.domain.SignalDomain;

import java.util.Map;
import java.util.function.Function;

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
 * @see FormulaVisitor
 * @see SpatialTemporalMonitor
 */
public class SpatialTemporalMonitoring<S, T, R> implements
        FormulaVisitor<Parameters, SpatialTemporalMonitor<S, T, R>>
{

    private final Map<String, Function<Parameters, Function<T, R>>> atomicPropositions;

    private final Map<String, Function<SpatialModel<S>, DefaultDistanceStructure<S, ?>>> distanceFunctions;

    private final SignalDomain<R> module;

    private final boolean staticSpace;


    public SpatialTemporalMonitor<S, T, R> monitor(Formula f,
                                                   Parameters parameters)
    {
        return f.accept(this, parameters);
    }

    /**
     * @param atomicPropositions
     * @param module
     */
    public SpatialTemporalMonitoring(
            Map<String, Function<Parameters, Function<T, R>>> atomicPropositions,
            Map<String, Function<SpatialModel<S>,
                    DefaultDistanceStructure<S, ?>>> distanceFunctions,
            SignalDomain<R> module,
            boolean staticSpace)
    {
        super();
        this.atomicPropositions = atomicPropositions;
        this.module = module;
        this.distanceFunctions = distanceFunctions;
        this.staticSpace = staticSpace;
    }

    /**
     * @see FormulaVisitor#visit(AtomicFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S,T,R> visit(
            AtomicFormula atomicFormula, Parameters parameters) {
        Function<Parameters, Function<T, R>> f = atomicPropositions.get(atomicFormula.getAtomicId());
        if (f == null) {
            throw new IllegalArgumentException("Unkown atomic ID " + atomicFormula.getAtomicId());
        }
        Function<T, R> atomic = f.apply(parameters);
        return SpatialTemporalMonitor.atomicMonitor(atomic);
    }

    /**
     * @see FormulaVisitor#visit(AndFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S,T,R> visit(AndFormula andFormula, Parameters parameters) {
        SpatialTemporalMonitor<S,T,R> leftMonitoring = andFormula.getFirstArgument().accept(this, parameters);
        SpatialTemporalMonitor<S,T,R> rightMonitoring = andFormula.getSecondArgument().accept(this, parameters);
        return SpatialTemporalMonitor.andMonitor(leftMonitoring, module, rightMonitoring);
    }

    /**
     * @see FormulaVisitor#visit(NegationFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S,T,R> visit(
            NegationFormula negationFormula, Parameters parameters) {
        SpatialTemporalMonitor<S,T,R> m = negationFormula.getArgument().accept(this, parameters);
        return SpatialTemporalMonitor.notMonitor(m, module);
    }

    /**
     * @see FormulaVisitor#visit(OrFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S,T,R> visit(OrFormula orFormula, Parameters parameters) {
        SpatialTemporalMonitor<S,T,R> leftMonitoring = orFormula.getFirstArgument().accept(this, parameters);
        SpatialTemporalMonitor<S,T,R> rightMonitoring = orFormula.getSecondArgument().accept(this, parameters);
        return SpatialTemporalMonitor.orMonitor(leftMonitoring, module, rightMonitoring);
    }

    /**
     * @see FormulaVisitor#visit(EventuallyFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S,T,R> visit(
            EventuallyFormula eventuallyFormula, Parameters parameters) {
        SpatialTemporalMonitor<S,T,R> m = eventuallyFormula.getArgument().accept(this, parameters);
        return SpatialTemporalMonitor.eventuallyMonitor(m,eventuallyFormula.getInterval(),module);
    }

    /**
     * @see FormulaVisitor#visit(GloballyFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S,T,R> visit(
            GloballyFormula globallyFormula, Parameters parameters) {
        SpatialTemporalMonitor<S,T,R> m = globallyFormula.getArgument().accept(this, parameters);
        return SpatialTemporalMonitor.globallyMonitor(m, globallyFormula.getInterval(),module);
    }

    /**
     * @see FormulaVisitor#visit(UntilFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S,T,R> visit(
            UntilFormula untilFormula, Parameters parameters) {
        SpatialTemporalMonitor<S,T,R> firstMonitoring = untilFormula.getFirstArgument().accept(this, parameters);
        SpatialTemporalMonitor<S,T,R> secondMonitoring = untilFormula.getSecondArgument().accept(this, parameters);
        return SpatialTemporalMonitor.untilMonitor(firstMonitoring, untilFormula.getInterval(), secondMonitoring, module);
    }

    /**
     * @see FormulaVisitor#visit(SinceFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S,T,R> visit(
            SinceFormula sinceFormula, Parameters parameters) {
        SpatialTemporalMonitor<S,T,R> firstMonitoring = sinceFormula.getFirstArgument().accept(this, parameters);
        SpatialTemporalMonitor<S,T,R> secondMonitoring = sinceFormula.getSecondArgument().accept(this, parameters);
        return SpatialTemporalMonitor.sinceMonitor(firstMonitoring, sinceFormula.getInterval(), secondMonitoring, module);
    }

    /**
     * @see FormulaVisitor#visit(HistoricallyFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S,T,R> visit(
            HistoricallyFormula historicallyFormula, Parameters parameters) {
        SpatialTemporalMonitor<S,T,R> argumentMonitoring = historicallyFormula.getArgument().accept(this, parameters);
        return SpatialTemporalMonitor.historicallyMonitor(argumentMonitoring, historicallyFormula.getInterval(),module);
    }

    /**
     * @see FormulaVisitor#visit(OnceFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S,T,R> visit(
            OnceFormula onceFormula, Parameters parameters) {
        SpatialTemporalMonitor<S,T,R> argumentMonitoring = onceFormula.getArgument().accept(this, parameters);
        return SpatialTemporalMonitor.onceMonitor(argumentMonitoring, onceFormula.getInterval(), module);
    }

    /**
     * @see FormulaVisitor#visit(SomewhereFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S,T,R> visit(
            SomewhereFormula somewhereFormula, Parameters parameters) {
        Function<SpatialModel<S>, DefaultDistanceStructure<S, ?>> distanceFunction = distanceFunctions.get(somewhereFormula.getDistanceFunctionId());
        SpatialTemporalMonitor<S,T,R> argumentMonitor = somewhereFormula.getArgument().accept(this, parameters);
        return SpatialTemporalMonitor.somewhereMonitor(argumentMonitor, distanceFunction, module);
    }

    /**
     * @see FormulaVisitor#visit(EverywhereFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S,T,R> visit(
            EverywhereFormula everywhereFormula, Parameters parameters) {
        Function<SpatialModel<S>, DefaultDistanceStructure<S, ?>> distanceFunction = distanceFunctions.get(everywhereFormula.getDistanceFunctionId());
        SpatialTemporalMonitor<S,T,R> argumentMonitor = everywhereFormula.getArgument().accept(this, parameters);
        return SpatialTemporalMonitor.everywhereMonitor(argumentMonitor, distanceFunction, module);
    }


    /**
     * @see FormulaVisitor#visit(ReachFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S,T,R> visit(
            ReachFormula reachFormula, Parameters parameters) {
        Function<SpatialModel<S>, DefaultDistanceStructure<S, ?>> distanceFunction = distanceFunctions.get(reachFormula.getDistanceFunctionId());
        SpatialTemporalMonitor<S,T,R> m1 = reachFormula.getFirstArgument().accept(this, parameters);
        SpatialTemporalMonitor<S,T,R> m2 = reachFormula.getSecondArgument().accept(this, parameters);
        return SpatialTemporalMonitor.reachMonitor(m1, distanceFunction, m2, module);
    }


    /**
     * @see FormulaVisitor#visit(EscapeFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S,T,R> visit(
            EscapeFormula escapeFormula, Parameters parameters) {
        Function<SpatialModel<S>, DefaultDistanceStructure<S, ?>> distanceFunction = distanceFunctions.get(escapeFormula.getDistanceFunctionId());
        SpatialTemporalMonitor<S,T,R> argumentMonitor = escapeFormula.getArgument().accept(this, parameters);
        return SpatialTemporalMonitor.escapeMonitor(argumentMonitor, distanceFunction, module);
    }




}
