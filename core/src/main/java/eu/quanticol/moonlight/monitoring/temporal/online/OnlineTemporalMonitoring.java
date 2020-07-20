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
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @param <T>
 * @param <R>
 */
public class OnlineTemporalMonitoring<T, R> implements
        FormulaVisitor<Parameters, TemporalMonitor<T, R>>
{
    private final Map<String, Function<Parameters, Function<T, R>>> atoms;
    private final SignalDomain<R> domain;

    private final Map<String, TemporalMonitor<T, R>> monitors;

    /**
     * Initializes a monitoring process over the given interpretation domain.
     * @param interpretation signal interpretation domain
     */
    public OnlineTemporalMonitoring(SignalDomain<R> interpretation) {
        this(new HashMap<>(), interpretation);
    }

    /**
     * Initializes a monitoring process over the given interpretation domain,
     * and the given atomic propositions.
     * @param atomicPropositions atomic propositions of interest
     * @param interpretation signal interpretation domain
     */
    public OnlineTemporalMonitoring(
           Map<String, Function<Parameters, Function<T, R>>> atomicPropositions,
           SignalDomain<R> interpretation)
    {
        this.atoms = atomicPropositions;
        this.domain = interpretation;
        this.monitors = new HashMap<>();
    }

    public String debug() {
        StringBuilder output = new StringBuilder();
        for(Map.Entry<String, TemporalMonitor<T, R>> e: monitors.entrySet()) {
            output.append("Monitor ").append(e.getKey()).append(":\n");
            output.append(((OnlineTemporalMonitor<T, R>) e.getValue())
                                                          .getWorklist());
            output.append("\n");
        }

        return output.toString();
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
            m = new OnlineMonitorAtomic<>(atomic, getHorizon(parameters));
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
        TemporalMonitor<T, R> m = monitors.get(andFormula.toString());

        TemporalMonitor<T, R> leftMonitoring = andFormula
                                              .getFirstArgument()
                                              .accept(this, parameters);
        TemporalMonitor<T, R> rightMonitoring = andFormula
                                               .getSecondArgument()
                                               .accept(this, parameters);
        if(m == null) {
            m = new OnlineMonitorBinaryOperator<>(leftMonitoring,
                                                  domain::conjunction,
                                                  rightMonitoring,
                                                  getHorizon(parameters));
            monitors.put(andFormula.toString(), m);
        }
        return m;
    }

    @Override
    public TemporalMonitor<T, R> visit(OrFormula orFormula,
                                       Parameters parameters)
    {
        TemporalMonitor<T, R> m = monitors.get(orFormula.toString());

        TemporalMonitor<T, R> leftMonitoring = orFormula
                                              .getFirstArgument()
                                              .accept(this, parameters);
        TemporalMonitor<T, R> rightMonitoring = orFormula
                                               .getSecondArgument()
                                               .accept(this, parameters);
        if(m == null) {
            m = new OnlineMonitorBinaryOperator<>(leftMonitoring,
                                                  domain::disjunction,
                                                  rightMonitoring,
                                                  getHorizon(parameters));
            monitors.put(orFormula.toString(), m);
        }
        return m;
    }

    @Override
    public TemporalMonitor<T, R> visit(EventuallyFormula eventuallyFormula,
                                       Parameters parameters)
    {
        TemporalMonitor<T, R> m = monitors.get(eventuallyFormula.toString());

        Interval horizon = getHorizon(parameters);
        Interval interval = eventuallyFormula.getInterval();
        horizon = Interval.combine(horizon, interval);

        TemporalMonitor<T, R> monitoringArg = eventuallyFormula
                                             .getArgument()
                                             .accept(this, parameters);

        if(m == null) {
            if (eventuallyFormula.isUnbounded()) {
                m = new OnlineMonitorFutureOperator<>(monitoringArg,
                                                      domain::disjunction,
                                                      domain.min(),
                                                      horizon);
            } else {
                m = new OnlineMonitorFutureOperator<>(monitoringArg,
                                                      domain::disjunction,
                                                      domain.min(),
                                                      interval,
                                                      horizon);
            }
            monitors.put(eventuallyFormula.toString(), m);
        }
        return m;
    }

    @Override
    public TemporalMonitor<T, R> visit(GloballyFormula globallyFormula,
                                       Parameters parameters)
    {
        TemporalMonitor<T, R> m = monitors.get(globallyFormula.toString());

        Interval horizon = getHorizon(parameters);
        Interval interval = globallyFormula.getInterval();
        horizon = Interval.combine(horizon, interval);

        TemporalMonitor<T, R> monitoringArg = globallyFormula
                                             .getArgument()
                                             .accept(this, parameters);

        if(m == null) {
            if (globallyFormula.isUnbounded()) {
                m = new OnlineMonitorFutureOperator<>(monitoringArg,
                                                      domain::conjunction,
                                                      domain.max(),
                                                      horizon);
            } else {
                m = new OnlineMonitorFutureOperator<>(monitoringArg,
                                                      domain::conjunction,
                                                      domain.max(),
                                                      interval,
                                                      horizon);
            }
            monitors.put(globallyFormula.toString(), m);
        }
        return m;
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

    @Override
    public TemporalMonitor<T, R> visit(HistoricallyFormula historicallyFormula,
                                       Parameters parameters)
    {
        TemporalMonitor<T, R> monitoringArg = historicallyFormula
                .getArgument()
                .accept(this, parameters);

        if (historicallyFormula.isUnbounded()) {
            return historicallyMonitor(monitoringArg, domain);
        } else {
            return historicallyMonitor(monitoringArg, domain,
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
            return onceMonitor(monitoringArg, domain);
        } else {
            Interval interval = onceFormula.getInterval();
            return onceMonitor(monitoringArg, domain, interval);
        }
    }
     */


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
