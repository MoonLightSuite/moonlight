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

package eu.quanticol.moonlight.monitoring.temporal.online;

import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.TemporalMonitoring;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * Primary entry point to perform online monitoring.
 * The key difference is that it is based on a visitor
 * design pattern over the formula tree, just like {@link TemporalMonitoring}.
 *
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see TemporalMonitoring
 * @see FormulaVisitor
 * @see TemporalMonitor
 */
public class LegacyOnlineTemporalMonitoring<T, R>
        implements FormulaVisitor<Parameters, TemporalMonitor<T, R>>
{
    private final Map<String, Function<Parameters, Function<T, R>>> atoms;
    private final SignalDomain<R> domain;

    private final Map<String, TemporalMonitor<T, R>> monitors;

    /**
     * Initializes a monitoring process over the given interpretation domain.
     * @param interpretation signal interpretation domain
     */
    public LegacyOnlineTemporalMonitoring(SignalDomain<R> interpretation) {
        this(new HashMap<>(), interpretation);
    }

    /**
     * Initializes a monitoring process over the given interpretation domain,
     * and the given atomic propositions.
     * @param atomicPropositions atomic propositions of interest
     * @param interpretation signal interpretation domain
     */
    public LegacyOnlineTemporalMonitoring(
           Map<String, Function<Parameters, Function<T, R>>> atomicPropositions,
           SignalDomain<R> interpretation)
    {
        this.atoms = atomicPropositions;
        this.domain = interpretation;
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
    public TemporalMonitor<T, R> monitor(Formula f,
                                         Parameters params)
    {
        /*
        if(end >= this.end)
            this.end = end;
        else
            throw new UnsupportedOperationException("Backward monitoring is " +
                                                    "not allowed!");
        */
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


    @Override
    public TemporalMonitor<T, R> visit(AtomicFormula atomicFormula,
                                       Parameters parameters)
    {
        TemporalMonitor<T, R> m = monitors.get(atomicFormula.toString());

        Function<Parameters, Function<T, R>> f = fetchAtom(atomicFormula);
        Function<T, R> atomic = f.apply(parameters);

        if(m == null) {
            m = new OnlineMonitorAtomic<>(atomic,
                                          getHorizon(parameters),
                                          domain.any());
            monitors.put(atomicFormula.toString(), m);
        }
        return m;
    }

    @Override
    public TemporalMonitor<T, R> visit(NegationFormula negationFormula,
                                       Parameters parameters)
    {
        TemporalMonitor<T, R> m = monitors.get(negationFormula.toString());

        TemporalMonitor<T, R> monitoringArg = negationFormula.getArgument()
                                             .accept(this, parameters);

        if(m == null) {
            m = new OnlineMonitorUnaryOperator<>(monitoringArg,
                                                 domain::negation,
                                                 getHorizon(parameters));
            monitors.put(negationFormula.toString(), m);
        }
        return m;
    }

    @Override
    public TemporalMonitor<T, R> visit(AndFormula andFormula,
                                       Parameters parameters)
    {
        return binaryMonitor(andFormula, parameters, domain::conjunction);
    }

    @Override
    public TemporalMonitor<T, R> visit(OrFormula orFormula,
                                       Parameters parameters)
    {
        return binaryMonitor(orFormula, parameters, domain::disjunction);
    }

    @Override
    public TemporalMonitor<T, R> visit(EventuallyFormula eventuallyFormula,
                                       Parameters parameters)
    {
        return unaryTemporalMonitor(eventuallyFormula, parameters,
                                    domain::disjunction, domain.min());
    }

    @Override
    public TemporalMonitor<T, R> visit(GloballyFormula globallyFormula,
                                       Parameters parameters)
    {
        return unaryTemporalMonitor(globallyFormula, parameters,
                                    domain::conjunction, domain.max());
    }

    @Override
    public TemporalMonitor<T, R> visit(HistoricallyFormula historicallyFormula,
                                       Parameters parameters)
    {
        return unaryTemporalMonitor(historicallyFormula, parameters,
                                    domain::conjunction, domain.max());
    }

    @Override
    public TemporalMonitor<T, R> visit(OnceFormula onceFormula,
                                       Parameters parameters)
    {
        return unaryTemporalMonitor(onceFormula, parameters,
                                    domain::disjunction, domain.min());
    }

/*
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
            return untilMonitor(firstMonitoring, secondMonitoring, domain);
        } else {
            return untilMonitor(firstMonitoring, untilFormula.getInterval(),
                    secondMonitoring, domain);
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
            return sinceMonitor(firstMonitoring, secondMonitoring, domain);
        } else {
            return sinceMonitor(firstMonitoring, sinceFormula.getInterval(),
                    secondMonitoring, domain);
        }
    }
     */


    private TemporalMonitor<T, R> binaryMonitor(BinaryFormula f, Parameters ps,
                                                BinaryOperator<R> op)
    {
        TemporalMonitor<T, R> m = monitors.get(f.toString());

        TemporalMonitor<T, R> leftMonitoring = f.getFirstArgument()
                                                .accept(this, ps);
        TemporalMonitor<T, R> rightMonitoring = f.getSecondArgument()
                                                 .accept(this, ps);

        if(m == null) {
            m = new OnlineMonitorBinaryOperator<>(leftMonitoring,
                    op,
                    rightMonitoring,
                    getHorizon(ps));
            monitors.put(f.toString(), m);
        }
        return m;
    }

    private TemporalMonitor<T, R> unaryTemporalMonitor(TemporalFormula f,
                                                       Parameters ps,
                                                       BinaryOperator<R> op,
                                                       R min)
    {
        TemporalMonitor<T, R> m = monitors.get(f.toString());

        Interval horizon = getHorizon(ps);
        Interval interval = f.getInterval();
        Parameters childPars = new HorizonParameter(
                Interval.combine(horizon, interval));

        TemporalMonitor<T, R> monitoringArg = ((UnaryFormula) f).getArgument()
                                               .accept(this, childPars);

        if(m == null) {
            m = new OnlineMonitorFutureOperator<>(monitoringArg,
                                                  op, min, domain.any(),
                                                  interval, horizon);
            monitors.put(f.toString(), m);
        }
        return m;
    }

    private Interval getHorizon(Parameters params) {
        if(params instanceof HorizonParameter) {
            return ((HorizonParameter) params).getHorizon();
        }

        throw new InvalidParameterException("Monitoring parameters are " +
                "incorrect: " + params.toString());
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
