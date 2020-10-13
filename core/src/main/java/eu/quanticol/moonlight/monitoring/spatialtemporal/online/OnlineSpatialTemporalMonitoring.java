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

package eu.quanticol.moonlight.monitoring.spatialtemporal.online;

import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.SpatialTemporalMonitoring;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.monitoring.temporal.online.*;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.SpatialModel;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor.*;

/**
 * Primary entry point to perform online (spatial) monitoring.
 * The key difference is that it is based on a visitor
 * design pattern over the formula tree, just like
 * {@link SpatialTemporalMonitoring}.
 *
 * Note: Particularly useful in static environment.
 *
 * @param <S> Spatial Graph Edge Type
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see SpatialTemporalMonitoring
 * @see FormulaVisitor
 * @see SpatialTemporalMonitor
 */
public class OnlineSpatialTemporalMonitoring<S, T, R> implements
        FormulaVisitor<Parameters, SpatialTemporalMonitor<S, T, R>>
{

    private final Map<String, Function<Parameters, Function<T, R>>> atoms;
    private final SignalDomain<R> domain;
    private final Map<String, Function<SpatialModel<S>,
                              DistanceStructure<S, ?>>> distanceFunctions;
    private final boolean staticSpace;

    private final Map<String, SpatialTemporalMonitor<S, T, R>> monitors;




    /**
     * Initializes a monitoring process over the given interpretation domain,
     * the given distance functions, and the given atomic propositions.
     * @param atomicPropositions atomic propositions of interest
     * @param distanceFunctions set of distance functions of interest
     * @param interpretation signal interpretation domain
     * @param staticSpace optional parameter for efficiency reasons *[UNUSED]*
     */
    public OnlineSpatialTemporalMonitoring(
           Map<String, Function<Parameters, Function<T, R>>> atomicPropositions,
           Map<String, Function<SpatialModel<S>,
           DistanceStructure<S, ?>>> distanceFunctions,
           SignalDomain<R> interpretation,
           boolean staticSpace)
    {
        this.atoms = atomicPropositions;
        this.domain = interpretation;
        this.distanceFunctions = distanceFunctions;
        this.staticSpace = staticSpace;
        this.monitors = new HashMap<>();
    }

    /**
     * Entry point of the monitoring program:
     * It launches the monitoring process over the formula f.
     * The Parameters class is required to provide the monitoring horizon.
     * If not provided, it is generated internally.
     *
     * @param f the formula to monitor
     * @param params monitoring parameters
     * @return the result of the monitoring process.
     */
    public SpatialTemporalMonitor<S, T, R> monitor(Formula f,
                                                   Parameters params)
    {
        HorizonParameter horizon;
        try {
            getHorizon(params);
            horizon = (HorizonParameter) params;
        }
        catch(Exception e) {
            horizon = new HorizonParameter(new Interval(0));
        }
        return f.accept(this, horizon);
    }

    /**
     * @see FormulaVisitor#visit(AtomicFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S, T, R> visit(AtomicFormula atomicF,
                                                 Parameters parameters)
    {
        SpatialTemporalMonitor<S, T, R> m = monitors.get(atomicF.toString());
        
        Function<Parameters, Function<T, R>> f = fetchAtom(atomicF);
        Function<T, R> atomic = f.apply(parameters);

        if(m == null) {
            m = new OnlineSTMonitorAtomic<>(atomic,
                                            getHorizon(parameters),
                                            domain.unknown());
            monitors.put(atomicF.toString(), m);
        }
        return m;
    }

    /**
     * @see FormulaVisitor#visit(NegationFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S, T, R> visit(NegationFormula negationF,
                                                 Parameters parameters)
    {
        SpatialTemporalMonitor<S, T, R> m = monitors.get(negationF.toString());

        SpatialTemporalMonitor<S, T, R> monitoringArg =
                        negationF.getArgument().accept(this, parameters);

        if(m == null) {
            m = new OnlineSTMonitorUnaryOperator<>(monitoringArg,
                                                   domain::negation,
                                                   getHorizon(parameters));
            monitors.put(negationF.toString(), m);
        }
        return m;
    }

    /**
     * @see FormulaVisitor#visit(AndFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S, T, R> visit(AndFormula andFormula,
                                                 Parameters parameters)
    {
        return binaryMonitor(andFormula, parameters, domain::conjunction);
    }

    /**
     * @see FormulaVisitor#visit(OrFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S, T, R> visit(OrFormula orFormula,
                                                 Parameters parameters)
    {
        return binaryMonitor(orFormula, parameters, domain::disjunction);
    }

    /**
     * @see FormulaVisitor#visit(EventuallyFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S, T, R> visit(EventuallyFormula eventually,
                                                 Parameters parameters)
    {
        return unaryTemporalMonitor(eventually, parameters,
                                    domain::disjunction, domain.min());
    }

    /**
     * @see FormulaVisitor#visit(GloballyFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S, T, R> visit(GloballyFormula globally,
                                                 Parameters parameters)
    {
        return unaryTemporalMonitor(globally, parameters,
                                    domain::conjunction, domain.max());
    }

    /**
     * @see FormulaVisitor#visit(HistoricallyFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S, T, R> visit(HistoricallyFormula historicF,
                                               Parameters parameters)
    {
        return unaryTemporalMonitor(historicF, parameters,
                                    domain::conjunction, domain.max());
    }

    /**
     * @see FormulaVisitor#visit(OnceFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S, T, R> visit(OnceFormula onceFormula,
                                                 Parameters parameters)
    {
        return unaryTemporalMonitor(onceFormula, parameters,
                                    domain::disjunction, domain.min());
    }

    /*
    @Override
    public SpatialTemporalMonitor<S, T, R> visit(UntilFormula untilFormula,
                                                 Parameters parameters)
    {
        SpatialTemporalMonitor<S, T, R> firstMonitoring = untilFormula.getFirstArgument().accept(this, parameters);
        SpatialTemporalMonitor<S, T, R> secondMonitoring = untilFormula.getSecondArgument().accept(this, parameters);
        return untilMonitor(firstMonitoring, untilFormula.getInterval(), secondMonitoring, domain);
    }

    @Override
    public SpatialTemporalMonitor<S, T, R> visit(SinceFormula sinceFormula,
                                               Parameters parameters) {
        SpatialTemporalMonitor<S, T, R> firstMonitoring = sinceFormula.getFirstArgument().accept(this, parameters);
        SpatialTemporalMonitor<S, T, R> secondMonitoring = sinceFormula.getSecondArgument().accept(this, parameters);
        return sinceMonitor(firstMonitoring, sinceFormula.getInterval(), secondMonitoring, domain);
    }
    */

    /**
     * @see FormulaVisitor#visit(SomewhereFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S, T, R> visit(SomewhereFormula somewhere,
                                               Parameters parameters)
    {
        Function<SpatialModel<S>, DistanceStructure<S, ?>> distanceFunction = distanceFunctions.get(somewhere.getDistanceFunctionId());
        SpatialTemporalMonitor<S, T, R> argumentMonitor = somewhere.getArgument().accept(this, parameters);
        return somewhereMonitor(argumentMonitor, distanceFunction, domain);
    }

    /**
     * @see FormulaVisitor#visit(EverywhereFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S, T, R> visit(EverywhereFormula everywhere,
                                               Parameters parameters)
    {
        Function<SpatialModel<S>, DistanceStructure<S, ?>> distanceFunction = distanceFunctions.get(everywhere.getDistanceFunctionId());
        SpatialTemporalMonitor<S, T, R> argumentMonitor = everywhere.getArgument().accept(this, parameters);
        return everywhereMonitor(argumentMonitor, distanceFunction, domain);
    }


    /**
     * @see FormulaVisitor#visit(ReachFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S, T, R> visit(ReachFormula reachFormula,
                                               Parameters parameters)
    {
        Function<SpatialModel<S>, DistanceStructure<S, ?>> distanceFunction = distanceFunctions.get(reachFormula.getDistanceFunctionId());
        SpatialTemporalMonitor<S, T, R> m1 = reachFormula.getFirstArgument().accept(this, parameters);
        SpatialTemporalMonitor<S, T, R> m2 = reachFormula.getSecondArgument().accept(this, parameters);
        return reachMonitor(m1, distanceFunction, m2, domain);
    }


    /**
     * @see FormulaVisitor#visit(EscapeFormula, Object)
     */
    @Override
    public SpatialTemporalMonitor<S, T, R> visit(EscapeFormula escapeFormula,
                                               Parameters parameters)
    {
        Function<SpatialModel<S>, DistanceStructure<S, ?>> distanceFunction = distanceFunctions.get(escapeFormula.getDistanceFunctionId());
        SpatialTemporalMonitor<S, T, R> argumentMonitor = escapeFormula.getArgument().accept(this, parameters);
        return escapeMonitor(argumentMonitor, distanceFunction, domain);
    }

    private Interval getHorizon(Parameters params) {
        if(params instanceof HorizonParameter) {
            return ((HorizonParameter) params).getHorizon();
        }

        throw new InvalidParameterException("Monitoring parameters are " +
                "incorrect: " + params.toString());
    }

    private SpatialTemporalMonitor<S, T, R> binaryMonitor(BinaryFormula f,                                                                 Parameters ps,
                                                          BinaryOperator<R> op)
    {
        SpatialTemporalMonitor<S, T, R> m = monitors.get(f.toString());

        SpatialTemporalMonitor<S, T, R> leftMonitoring = f.getFirstArgument()
                .accept(this, ps);
        SpatialTemporalMonitor<S, T, R> rightMonitoring = f.getSecondArgument()
                .accept(this, ps);

        if(m == null) {
            m = new OnlineSTMonitorBinaryOperator<>(leftMonitoring,
                                                    op,
                                                    rightMonitoring,
                                                    getHorizon(ps));
            monitors.put(f.toString(), m);
        }
        return m;
    }

    private SpatialTemporalMonitor<S, T, R> unaryTemporalMonitor(
                                                        TemporalFormula f,
                                                        Parameters ps,
                                                        BinaryOperator<R> op,
                                                        R min)
    {
        SpatialTemporalMonitor<S, T, R> m = monitors.get(f.toString());

        Interval horizon = getHorizon(ps);
        Interval interval = f.getInterval();
        Parameters childPars = new HorizonParameter(
                                           Interval.combine(horizon, interval));

        SpatialTemporalMonitor<S, T, R> monitoringArg = ((UnaryFormula) f)
                                                .getArgument()
                                                .accept(this, childPars);

        if(m == null) {
            m = new OnlineSTMonitorFutureOperator<>(monitoringArg,
                                                    op, min, domain.unknown(),
                                                    interval, horizon);
            monitors.put(f.toString(), m);
        }
        return m;
    }

    private Function<Parameters, Function<T, R>> fetchAtom(AtomicFormula f){
        Function<Parameters, Function<T, R>> atom = atoms.get(f.getAtomicId());

        if(atom == null) {
            throw new IllegalArgumentException("Unknown atomic ID " +
                    f.getAtomicId());
        }
        return atom;
    }


}
