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
import eu.quanticol.moonlight.online.algorithms.SpatialComputation;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.online.monitoring.OnlineMonitor;
import eu.quanticol.moonlight.online.signal.OnlineSpaceTimeSignal;
import eu.quanticol.moonlight.online.signal.TimeChain;
import eu.quanticol.moonlight.core.signal.TimeSignal;
import eu.quanticol.moonlight.online.signal.Update;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategy to interpret the unary spatial logic operators.
 *
 * @param <S> Spatial Graph Edge Type
 * @param <V> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see OnlineMonitor
 */
public class UnarySpaceOpMonitor<S, V, R extends Comparable<R>>
        implements OnlineMonitor<Double, List<V>, List<Box<R>>>
{
    private final OnlineMonitor<Double, List<V>,
                                List<Box<R>>> argument;
    private final TimeSignal<Double, List<Box<R>>> rho;
    private final SpatialComputation<Double, S, Box<R>> spatialOp;

    public UnarySpaceOpMonitor(
            OnlineMonitor<Double, List<V>, List<Box<R>>> argument,
            SpatialComputation<Double, S, Box<R>> op,
            SignalDomain<R> domain,
            int size)
    {
        this.argument = argument;
        this.rho = new OnlineSpaceTimeSignal<>(size, domain);
        this.spatialOp = op;
    }

    @Override
    public List<TimeChain<Double, List<Box<R>>>> monitor(
            Update<Double, List<V>> signalUpdate)
    {
        List<TimeChain<Double, List<Box<R>>>> argUpdates =
                argument.monitor(signalUpdate);

        List<TimeChain<Double, List<Box<R>>>> updates =
                                                              new ArrayList<>();

        for(TimeChain<Double, List<Box<R>>> argU : argUpdates) {
            updates.add(spatialOp.computeUnaryChain(argU));
        }

        updates.forEach(rho::refine);

        return updates;
    }

    @Override
    public List<TimeChain<Double, List<Box<R>>>> monitor(
            TimeChain<Double, List<V>> updates)
    {
        List<TimeChain<Double, List<Box<R>>>> argUpdates =
                argument.monitor(updates);

        List<TimeChain<Double, List<Box<R>>>> output =
                new ArrayList<>();

        for(TimeChain<Double, List<Box<R>>> argU : argUpdates) {
            output.add(spatialOp.computeUnaryChain(argU));
        }

        for(TimeChain<Double, List<Box<R>>> us : output) {
            rho.refine(us);
        }

        return output;
    }

    @Override
    public TimeSignal<Double, List<Box<R>>> getResult() {
        return rho;
    }
}
