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
import eu.quanticol.moonlight.online.algorithms.ReachOperator;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.online.monitoring.OnlineMonitor;
import eu.quanticol.moonlight.online.signal.OnlineSpaceTimeSignal;
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
 * @see OnlineMonitor
 */
public class BinarySpaceOpMonitor<S, V, R extends Comparable<R>>
        implements OnlineMonitor<Double, List<V>, List<Box<R>>>
{

    private final BinaryOperator<List<Box<R>>> opFunction;
    private final OnlineSpaceTimeSignal<R> rho;
    private final OnlineMonitor<Double, List<V>, List<Box<R>>>
                                                                    firstArg;
    private final OnlineMonitor<Double, List<V>, List<Box<R>>>
                                                                    secondArg;

    private final ReachOperator<Double, S, Box<R>> spatialOp;

    /**
     * Prepares an atomic online (temporal) monitor.
     * @param binaryOp The function evaluated by the atomic predicate
     * //@param parentHorizon The temporal horizon of the parent formula
     * @param interpretation The interpretation domain of interest
     */
    public BinarySpaceOpMonitor(
            OnlineMonitor<Double, List<V>, List<Box<R>>> firstArg,
            OnlineMonitor<Double, List<V>, List<Box<R>>> secondArg,
            BinaryOperator<List<Box<R>>> binaryOp,
            SignalDomain<R> interpretation,
            int locations)
    {
        this.opFunction = binaryOp;
        this.rho = new OnlineSpaceTimeSignal<>(locations, interpretation);
        this.firstArg = firstArg;
        this.secondArg = secondArg;
        //this.spatialOp = new ReachOperator<>(locSvc, distance);
        this.spatialOp = null;

    }

    @Override
    public List<TimeChain<Double, List<Box<R>>>> monitor(
            Update<Double, List<V>> signalUpdate)
    {
        List<TimeChain<Double, List<Box<R>>>> updates =
                                                            new ArrayList<>();

        List<TimeChain<Double, List<Box<R>>>> firstArgUps =
                                                firstArg.monitor(signalUpdate);
        List<TimeChain<Double, List<Box<R>>>> secondArgUps =
                                                secondArg.monitor(signalUpdate);

        TimeSignal<Double, List<Box<R>>> s1 =
                                                        firstArg.getResult();
        TimeSignal<Double, List<Box<R>>> s2 =
                                                        secondArg.getResult();

        for(TimeChain<Double, List<Box<R>>> argU : firstArgUps) {
            //updates.addAll(spacialOp.binary(s2, argU, opFunction));
        }

        for(TimeChain<Double, List<Box<R>>> argU: secondArgUps) {
            //updates.addAll(SpatialAlgorithms.binary(s1, argU, opFunction));
        }

        updates.forEach(rho::refine);

        return updates;
    }

    @Override
    public List<TimeChain<Double, List<Box<R>>>> monitor(TimeChain<Double, List<V>> updates) {
        return null;
    }

    @Override
    public TimeSignal<Double, List<Box<R>>> getResult() {
        return rho;
    }
}
