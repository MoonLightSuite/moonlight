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

package io.github.moonlightsuite.moonlight.online.monitoring.spatialtemporal;

import io.github.moonlightsuite.moonlight.core.base.Box;
import io.github.moonlightsuite.moonlight.online.algorithms.BooleanOp;
import io.github.moonlightsuite.moonlight.core.signal.SignalDomain;
import io.github.moonlightsuite.moonlight.online.monitoring.OnlineMonitor;
import io.github.moonlightsuite.moonlight.offline.monitoring.temporal.TemporalMonitor;
import io.github.moonlightsuite.moonlight.online.signal.OnlineSpaceTimeSignal;
import io.github.moonlightsuite.moonlight.online.signal.TimeChain;
import io.github.moonlightsuite.moonlight.online.signal.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Strategy to interpret (online) an atomic predicate on the signal of interest.
 *
 * @param <V> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see TemporalMonitor
 */
public class AtomicMonitor<V, R extends Comparable<R>>
implements OnlineMonitor<Double, List<V>, List<Box<R>>>
{

    private final Function<List<V>, List<Box<R>>> atomicFunction;
    private final OnlineSpaceTimeSignal<R> rho;


    /**
     * Prepares an atomic online (temporal) monitor.
     * @param atomicFunction The function evaluated by the atomic predicate
     * //@param parentHorizon The temporal horizon of the parent formula
     * @param interpretation The interpretation domain of interest
     */
    public AtomicMonitor(Function<V, Box<R>> atomicFunction,
                         int locations,
                         SignalDomain<R> interpretation)
    {
        this.atomicFunction =  x -> x.stream().map(atomicFunction)
                                     .collect(Collectors.toList());
        this.rho = new OnlineSpaceTimeSignal<>(locations, interpretation);
    }

    @Override
    public List<TimeChain<Double, List<Box<R>>>> monitor(
            Update<Double, List<V>> signalUpdate)
    {
        TimeChain<Double, List<Box<R>>> u =
                BooleanOp.atomSequence(signalUpdate, atomicFunction);
        List<TimeChain<Double, List<Box<R>>>> updates =
                new ArrayList<>();
        updates.add(u);
        rho.refine(u);

        return updates;
    }

    @Override
    public List<TimeChain<Double, List<Box<R>>>> monitor(
            TimeChain<Double, List<V>> updates)
    {
        TimeChain<Double, List<Box<R>>> us =
                BooleanOp.atomSequence(updates, atomicFunction);

        List<TimeChain<Double, List<Box<R>>>> output = new ArrayList<>();
        output.add(us);
        rho.refine(us);

        return output;
    }

    @Override
    public OnlineSpaceTimeSignal<R> getResult() {
        return rho;
    }
}
