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

package eu.quanticol.moonlight.online.monitoring.temporal;

import eu.quanticol.moonlight.online.algorithms.TemporalComputation;
import eu.quanticol.moonlight.core.base.AbstractInterval;
import eu.quanticol.moonlight.core.formula.Interval;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.online.monitoring.OnlineMonitor;
import eu.quanticol.moonlight.offline.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.online.signal.OnlineSignal;
import eu.quanticol.moonlight.online.signal.TimeChain;
import eu.quanticol.moonlight.core.signal.TimeSignal;
import eu.quanticol.moonlight.online.signal.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;

/**
 * Strategy to interpret (online) an atomic predicate on the signal of interest.
 *
 * @param <V> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see TemporalMonitor
 */
public class TemporalOpMonitor<V, R extends Comparable<R>>
        implements OnlineMonitor<Double, V, AbstractInterval<R>>
{

    private final BinaryOperator<AbstractInterval<R>> op;
    private final Interval horizon;
    private final OnlineSignal<R> rho;
    private final OnlineMonitor<Double, V, AbstractInterval<R>> argumentMonitor;


    /**
     * Prepares an atomic online (temporal) monitor.
     * @param binaryOp The function evaluated by the atomic predicate
     * //@param parentHorizon The temporal horizon of the parent formula
     * @param interpretation The interpretation domain of interest
     */
    public TemporalOpMonitor(
                        OnlineMonitor<Double, V, AbstractInterval<R>> argument,
                        BinaryOperator<AbstractInterval<R>> binaryOp,
                        Interval timeHorizon,
                        //Interval parentHorizon,
                        SignalDomain<R> interpretation)
    {
        this.op = binaryOp;
        this.horizon = timeHorizon;
        this.rho = new OnlineSignal<>(interpretation);
        this.argumentMonitor = argument;
    }

    @Override
    public List<TimeChain<Double, AbstractInterval<R>>> monitor(
            Update<Double, V> signalUpdate)
    {
        List<TimeChain<Double, AbstractInterval<R>>> argUpdates =
                argumentMonitor.monitor(signalUpdate);

        TimeChain<Double, AbstractInterval<R>> s =
                argumentMonitor.getResult().getSegments();

        List<TimeChain<Double, AbstractInterval<R>>> updates = new ArrayList<>();

        for(TimeChain<Double, AbstractInterval<R>> argU: argUpdates) {
            updates.addAll(TemporalComputation.slidingWindow(s, argU,
                                                             horizon, op));
        }

        updates.forEach(rho::refine);

        return updates;
    }

    @Override
    public List<TimeChain<Double, AbstractInterval<R>>> monitor(
            TimeChain<Double, V> updates)
    {
        List<TimeChain<Double, AbstractInterval<R>>> argUpdates =
                argumentMonitor.monitor(updates);

        TimeChain<Double, AbstractInterval<R>> s =
                argumentMonitor.getResult().getSegments();

        List<TimeChain<Double, AbstractInterval<R>>> output = new ArrayList<>();

        for(TimeChain<Double, AbstractInterval<R>> argU: argUpdates) {
            output.addAll(TemporalComputation.slidingWindow(s, argU,
                    horizon, op));
        }

        output.forEach(rho::refine);

        return output;
    }
    @Override
    public TimeSignal<Double, AbstractInterval<R>> getResult() {
        return rho;
    }
}