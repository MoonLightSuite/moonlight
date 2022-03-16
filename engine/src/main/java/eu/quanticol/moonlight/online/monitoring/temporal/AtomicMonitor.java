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
import java.util.function.Function;

/**
 * Strategy to interpret (online) an atomic predicate on the signal of interest.
 *
 * @param <V> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see TemporalMonitor
 */
public class AtomicMonitor<V, R extends Comparable<R>>
        implements OnlineMonitor<Double, V, Box<R>>
{

    private final Function<V, Box<R>> atomicFunction;
    private final TimeSignal<Double, Box<R>> rho;


    /**
     * Prepares an atomic online (temporal) monitor.
     * @param atomicFunction The function evaluated by the atomic predicate
     * //@param parentHorizon The temporal horizon of the parent formula
     * @param interpretation The interpretation domain of interest
     */
    public AtomicMonitor(Function<V, Box<R>> atomicFunction,
                         SignalDomain<R> interpretation)
    {
        this.atomicFunction = atomicFunction;
        this.rho = new OnlineSignal<>(interpretation);
    }

    public List<Update<Double, Box<R>>> monitorOld(
            Update<Double, V> signalUpdate)
    {
        Update<Double, Box<R>> u =
                BooleanComputation.atom(signalUpdate, atomicFunction);
        List<Update<Double, Box<R>>> updates = new ArrayList<>();
        updates.add(u);
        rho.refine(u);

        return updates;
    }

    @Override
    public List<TimeChain<Double, Box<R>>> monitor(
            Update<Double, V> signalUpdate)
    {
        List<Update<Double, V>> updates  = new ArrayList<>();
        updates.add(signalUpdate);
        return monitor(Update.asTimeChain(updates));

    }

    @Override
    public List<TimeChain<Double, Box<R>>> monitor(
            TimeChain<Double, V> updates)
    {
        TimeChain<Double, Box<R>> us =
                BooleanComputation.atomSequence(updates, atomicFunction);

        List<TimeChain<Double, Box<R>>> output = new ArrayList<>();
        output.add(us);
        rho.refine(us);

        return output;
    }

    @Override
    public TimeSignal<Double, Box<R>> getResult() {
        return rho;
    }
}
