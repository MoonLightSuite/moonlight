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

package eu.quanticol.moonlight.monitoring.online.strategy.spacetime;

import eu.quanticol.moonlight.algorithms.online.SpatialComputation;
import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.monitoring.online.strategy.time.OnlineMonitor;
import eu.quanticol.moonlight.signal.online.OnlineSpaceTimeSignal;
import eu.quanticol.moonlight.signal.online.TimeSignal;
import eu.quanticol.moonlight.signal.online.Update;

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
        implements OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>>
{
    private final OnlineMonitor<Double, List<V>,
                                List<AbstractInterval<R>>> argument;
    private final TimeSignal<Double, List<AbstractInterval<R>>> rho;
    private final SpatialComputation<Double, S, AbstractInterval<R>> spatialOp;

    public UnarySpaceOpMonitor(
            OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> argument,
            SpatialComputation<Double, S, AbstractInterval<R>> op,
            SignalDomain<R> domain,
            int size)
    {
        this.argument = argument;
        this.rho = new OnlineSpaceTimeSignal<>(size, domain);
        this.spatialOp = op;
    }

    @Override
    public List<Update<Double, List<AbstractInterval<R>>>> monitor(
            Update<Double, List<V>> signalUpdate)
    {
        List<Update<Double, List<AbstractInterval<R>>>> argUpdates =
                argument.monitor(signalUpdate);

        List<Update<Double, List<AbstractInterval<R>>>> updates =
                                                              new ArrayList<>();

        for(Update<Double, List<AbstractInterval<R>>> argU : argUpdates) {
            updates.addAll(spatialOp.computeDynamic(argU));
        }

        for(Update<Double, List<AbstractInterval<R>>> u : updates) {
            rho.refine(u);
        }

        return updates;
    }

    @Override
    public TimeSignal<Double, List<AbstractInterval<R>>> getResult() {
        return rho;
    }
}
