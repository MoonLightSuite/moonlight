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

import eu.quanticol.moonlight.algorithms.online.BooleanComputation;
import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.monitoring.online.OnlineMonitor;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.online.OnlineSpaceTimeSignal;
import eu.quanticol.moonlight.signal.online.Update;

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
implements OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>>
{

    private final Function<List<V>, List<AbstractInterval<R>>> atomicFunction;
    private final OnlineSpaceTimeSignal<R> rho;


    /**
     * Prepares an atomic online (temporal) monitor.
     * @param atomicFunction The function evaluated by the atomic predicate
     * //@param parentHorizon The temporal horizon of the parent formula
     * @param interpretation The interpretation domain of interest
     */
    public AtomicMonitor(Function<V, AbstractInterval<R>> atomicFunction,
                         int locations,
                         SignalDomain<R> interpretation)
    {
        this.atomicFunction =  x -> x.stream().map(atomicFunction)
                                     .collect(Collectors.toList());
        this.rho = new OnlineSpaceTimeSignal<>(locations, interpretation);
    }

    @Override
    public List<Update<Double, List<AbstractInterval<R>>>> monitor(
            Update<Double, List<V>> signalUpdate)
    {
        Update<Double, List<AbstractInterval<R>>> u =
                BooleanComputation.atom(signalUpdate, atomicFunction);
        List<Update<Double, List<AbstractInterval<R>>>> updates =
                new ArrayList<>();
        updates.add(u);
        rho.refine(u);

        return updates;
    }

    @Override
    public OnlineSpaceTimeSignal<R> getResult() {
        return rho;
    }
}
