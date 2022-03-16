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

import eu.quanticol.moonlight.core.base.Box;
import eu.quanticol.moonlight.online.algorithms.BooleanComputation;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.online.monitoring.OnlineMonitor;
import eu.quanticol.moonlight.offline.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.online.signal.OnlineSignal;
import eu.quanticol.moonlight.online.signal.TimeChain;
import eu.quanticol.moonlight.core.signal.TimeSignal;
import eu.quanticol.moonlight.online.signal.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Strategy to interpret (online) an atomic predicate on the signal of interest.
 *
 * @param <V> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see TemporalMonitor
 */
public class UnaryMonitor<V, R extends Comparable<R>>
        implements OnlineMonitor<Double, V, Box<R>>
{

    private final UnaryOperator<Box<R>> op;
    private final TimeSignal<Double, Box<R>> rho;
    private final OnlineMonitor<Double, V, Box<R>> argumentMonitor;


    /**
     * Prepares an atomic online (temporal) monitor.
     * @param unaryOp The function evaluated by the atomic predicate
     * //@param parentHorizon The temporal horizon of the parent formula
     * @param interpretation The interpretation domain of interest
     */
    public UnaryMonitor(OnlineMonitor<Double, V, Box<R>> argument,
                        UnaryOperator<Box<R>> unaryOp,
                        SignalDomain<R> interpretation)
    {
        this.op = unaryOp;
        this.rho = new OnlineSignal<>(interpretation);
        this.argumentMonitor = argument;
    }

    @Override
    public List<TimeChain<Double, Box<R>>> monitor(
            Update<Double, V> signalUpdate)
    {
        List<TimeChain<Double, Box<R>>> argUpdates =
                                        argumentMonitor.monitor(signalUpdate);

        List<TimeChain<Double, Box<R>>> updates = new ArrayList<>();

        for(TimeChain<Double, Box<R>> argU : argUpdates) {
            updates.add(BooleanComputation.unarySequence(argU, op));
        }

        updates.forEach(rho::refine);

        return updates;
    }

    @Override
    public List<TimeChain<Double, Box<R>>> monitor(
            TimeChain<Double, V> updates)
    {
        List<TimeChain<Double, Box<R>>> argUpdates =
                argumentMonitor.monitor(updates);

        List<TimeChain<Double, Box<R>>> outputUpdates = new ArrayList<>();

        for(TimeChain<Double, Box<R>> argU : argUpdates) {
            outputUpdates.add(BooleanComputation.unarySequence(argU, op));
        }

        outputUpdates.forEach(rho::refine);

        return outputUpdates;
    }

    @Override
    public TimeSignal<Double, Box<R>> getResult() {
        return rho;
    }
}
