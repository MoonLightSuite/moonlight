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

package eu.quanticol.moonlight.monitoring.online.strategy.spacetime;

import eu.quanticol.moonlight.algorithms.online.TemporalComputation;
import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.monitoring.online.OnlineMonitor;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.online.*;

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
public class UnaryTimeOpMonitor<V, R extends Comparable<R>>
        implements OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>>
{

    private final BinaryOperator<List<AbstractInterval<R>>> op;
    private final Interval horizon;
    private final SpaceTimeSignal<Double, AbstractInterval<R>> rho;
    private final OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> argumentMonitor;


    /**
     * Prepares an atomic online (temporal) monitor.
     * @param binaryOp The function evaluated by the atomic predicate
     * //@param parentHorizon The temporal horizon of the parent formula
     * @param interpretation The interpretation domain of interest
     */
    public UnaryTimeOpMonitor(
                        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> argument,
                        BinaryOperator<List<AbstractInterval<R>>> binaryOp,
                        Interval timeHorizon,
                        //Interval parentHorizon,
                        SignalDomain<R> interpretation,
                        int locations)
    {
        this.op = binaryOp;
        this.horizon = timeHorizon;
        this.rho = new OnlineSpaceTimeSignal<>(locations, interpretation);
        this.argumentMonitor = argument;
    }

    @Override
    public List<TimeChain<Double, List<AbstractInterval<R>>>> monitor(
            Update<Double, List<V>> signalUpdate)
    {
        List<TimeChain<Double, List<AbstractInterval<R>>>> argUpdates =
                argumentMonitor.monitor(signalUpdate);

        TimeChain<Double, List<AbstractInterval<R>>> s =
                argumentMonitor.getResult().getSegments();

        List<TimeChain<Double, List<AbstractInterval<R>>>> updates = new ArrayList<>();

        for(TimeChain<Double, List<AbstractInterval<R>>> argU : argUpdates) {
            updates.addAll(TemporalComputation.slidingWindow(s,
                                                             argU,
                                                             horizon,
                                                             op));
        }

        for(TimeChain<Double, List<AbstractInterval<R>>> u: updates) {
            rho.refine(u);
        }

        return updates;
    }

    @Override
    public List<TimeChain<Double, List<AbstractInterval<R>>>> monitor(TimeChain<Double, List<V>> updates) {
        return null;
    }

    @Override
    public TimeSignal<Double, List<AbstractInterval<R>>> getResult() {
        return rho;
    }
}