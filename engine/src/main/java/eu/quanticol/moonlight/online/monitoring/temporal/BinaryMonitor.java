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
import eu.quanticol.moonlight.online.algorithms.BooleanOp;
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
import java.util.stream.Collectors;

/**
 * Strategy to interpret (online) an atomic predicate on the signal of interest.
 *
 * @param <V> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see TemporalMonitor
 */
public class BinaryMonitor<V, R extends Comparable<R>>
        implements OnlineMonitor<Double, V, Box<R>>
{

    private final BinaryOperator<Box<R>> opFunction;
    private final TimeSignal<Double, Box<R>> rho;
    private final OnlineMonitor<Double, V, Box<R>> firstArg;
    private final OnlineMonitor<Double, V, Box<R>> secondArg;


    /**
     * Prepares an atomic online (temporal) monitor.
     * @param binaryOp The function evaluated by the atomic predicate
     * //@param parentHorizon The temporal horizon of the parent formula
     * @param interpretation The interpretation domain of interest
     */
    public BinaryMonitor(
            OnlineMonitor<Double, V, Box<R>> firstArgument,
            OnlineMonitor<Double, V, Box<R>> secondArgument,
            BinaryOperator<Box<R>> binaryOp,
            SignalDomain<R> interpretation)
    {
        this.opFunction = binaryOp;
        this.rho = new OnlineSignal<>(interpretation);
        this.firstArg = firstArgument;
        this.secondArg = secondArgument;
    }

    @Override
    public List<TimeChain<Double, Box<R>>> monitor(
            Update<Double, V> signalUpdate)
    {
        List<TimeChain<Double, Box<R>>> updates = new ArrayList<>();

        List<TimeChain<Double, Box<R>>> firstArgUps =
                firstArg.monitor(signalUpdate).stream().filter(x -> !x.isEmpty()).collect(Collectors.toList());
        List<TimeChain<Double, Box<R>>> secondArgUps =
                secondArg.monitor(signalUpdate).stream().filter(x -> !x.isEmpty()).collect(Collectors.toList());


        TimeSignal<Double, Box<R>> s1 = firstArg.getResult();
        TimeSignal<Double, Box<R>> s2 = secondArg.getResult();

        for(TimeChain<Double, Box<R>> argU : firstArgUps) {
                TimeChain<Double, Box<R>> c2 =
                        s2.select(argU.getStart(), argU.getEnd());
                updates.add(BooleanOp.binarySequence(c2, argU, opFunction));
        }

        for(TimeChain<Double, Box<R>> argU: secondArgUps) {
                TimeChain<Double, Box<R>> c1 =
                        s1.select(argU.getStart(), argU.getEnd());
                updates.add(BooleanOp.binarySequence(c1, argU, opFunction));
        }

        updates.forEach(rho::refine);

        return updates;
    }

    /**
     * Execution starter of the monitoring process. It returns a list of update
     * sequences to the interpretation signal computed at the previous step.
     *
     * @param updates sequence of connected updates of the input signal
     * @return a list of updates to the interpretation signal
     */
    @Override
    public List<TimeChain<Double, Box<R>>>
    monitor(TimeChain<Double, V> updates)
    {
        List<TimeChain<Double, Box<R>>> output = new ArrayList<>();

        List<TimeChain<Double, Box<R>>> firstArgUps =
                firstArg.monitor(updates);
        List<TimeChain<Double, Box<R>>> secondArgUps =
                secondArg.monitor(updates);

        TimeSignal<Double, Box<R>> s1 = firstArg.getResult();
        TimeSignal<Double, Box<R>> s2 = secondArg.getResult();

        for(TimeChain<Double, Box<R>> argU : firstArgUps) {
            TimeChain<Double, Box<R>> c2 =
                    s2.select(argU.getStart(), argU.getEnd());
            output.add(BooleanOp.binarySequence(c2, argU, opFunction));
        }

        for(TimeChain<Double, Box<R>> argU: secondArgUps) {
            TimeChain<Double, Box<R>> c1 =
                    s1.select(argU.getStart(), argU.getEnd());
            output.add(BooleanOp.binarySequence(c1, argU, opFunction));
        }

        output.forEach(rho::refine);

        return output;
    }

    @Override
    public TimeSignal<Double, Box<R>> getResult() {
        return rho;
    }
}
