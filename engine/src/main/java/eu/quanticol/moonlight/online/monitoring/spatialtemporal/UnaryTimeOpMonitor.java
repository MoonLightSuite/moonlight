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

package eu.quanticol.moonlight.online.monitoring.spatialtemporal;

import eu.quanticol.moonlight.core.base.Box;
import eu.quanticol.moonlight.core.signal.SpaceTimeSignal;
import eu.quanticol.moonlight.core.signal.TimeSignal;
import eu.quanticol.moonlight.online.algorithms.TemporalComputation;
import eu.quanticol.moonlight.core.formula.Interval;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.online.monitoring.OnlineMonitor;
import eu.quanticol.moonlight.offline.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.online.signal.*;

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
        implements OnlineMonitor<Double, List<V>, List<Box<R>>>
{

    private final BinaryOperator<List<Box<R>>> op;
    private final Interval horizon;
    private final SpaceTimeSignal<Double, Box<R>> rho;
    private final OnlineMonitor<Double, List<V>, List<Box<R>>> argumentMonitor;


    /**
     * Prepares an atomic online (temporal) monitor.
     * @param binaryOp The function evaluated by the atomic predicate
     * //@param parentHorizon The temporal horizon of the parent formula
     * @param interpretation The interpretation domain of interest
     */
    public UnaryTimeOpMonitor(
                        OnlineMonitor<Double, List<V>, List<Box<R>>> argument,
                        BinaryOperator<List<Box<R>>> binaryOp,
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
    public List<TimeChain<Double, List<Box<R>>>> monitor(
            Update<Double, List<V>> signalUpdate)
    {
        List<TimeChain<Double, List<Box<R>>>> argUpdates =
                argumentMonitor.monitor(signalUpdate);

        TimeChain<Double, List<Box<R>>> s =
                argumentMonitor.getResult().getSegments();

        List<TimeChain<Double, List<Box<R>>>> updates = new ArrayList<>();

        for(TimeChain<Double, List<Box<R>>> argU : argUpdates) {
            updates.addAll(TemporalComputation.slidingWindow(s,
                                                             argU,
                                                             horizon,
                                                             op));
        }

        for(TimeChain<Double, List<Box<R>>> u: updates) {
            rho.refine(u);
        }

        return updates;
    }

    @Override
    public List<TimeChain<Double, List<Box<R>>>> monitor(TimeChain<Double, List<V>> updates) {
        List<TimeChain<Double, List<Box<R>>>> argUpdates =
                argumentMonitor.monitor(updates);

        TimeChain<Double, List<Box<R>>> s =
                argumentMonitor.getResult().getSegments();

        List<TimeChain<Double, List<Box<R>>>> result = new ArrayList<>();

        for(TimeChain<Double, List<Box<R>>> argU : argUpdates) {
            result.addAll(TemporalComputation.slidingWindow(s,
                                                            argU,
                                                            horizon,
                                                            op));
        }

        for(TimeChain<Double, List<Box<R>>> u: result) {
            rho.refine(u);
        }

        return result;
    }

    @Override
    public TimeSignal<Double, List<Box<R>>> getResult() {
        return rho;
    }
}