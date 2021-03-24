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

package eu.quanticol.moonlight.monitoring.online.strategy;

import eu.quanticol.moonlight.algorithms.online.BooleanComputation;
import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.monitoring.online.OnlineMonitor;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.monitoring.temporal.online.LegacyOnlineTemporalMonitoring;
import eu.quanticol.moonlight.signal.online.OnlineSignal;
import eu.quanticol.moonlight.signal.online.SignalInterface;
import eu.quanticol.moonlight.signal.online.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;

/**
 * Strategy to interpret (online) an atomic predicate on the signal of interest.
 *
 * @param <V> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see LegacyOnlineTemporalMonitoring
 * @see TemporalMonitor
 */
public class BinaryMonitor<V, R extends Comparable<R>>
        implements OnlineMonitor<Double, V, AbstractInterval<R>>
{

    private final BinaryOperator<AbstractInterval<R>> opFunction;
    //private final Interval horizon;
    private final SignalInterface<Double, AbstractInterval<R>> rho;
    private final OnlineMonitor<Double, V, AbstractInterval<R>> firstArgMonitor;
    private final OnlineMonitor<Double, V, AbstractInterval<R>> secondArgMonitor;


    /**
     * Prepares an atomic online (temporal) monitor.
     * @param binaryOp The function evaluated by the atomic predicate
     * //@param parentHorizon The temporal horizon of the parent formula
     * @param interpretation The interpretation domain of interest
     */
    public BinaryMonitor(
            OnlineMonitor<Double, V, AbstractInterval<R>> firstArgument,
            OnlineMonitor<Double, V, AbstractInterval<R>> secondArgument,
            BinaryOperator<AbstractInterval<R>> binaryOp,
            //Interval parentHorizon,
            SignalDomain<R> interpretation)
    {
        this.opFunction = binaryOp;
        //this.horizon = parentHorizon;
        this.rho = new OnlineSignal<>(interpretation);
        this.firstArgMonitor = firstArgument;
        this.secondArgMonitor = secondArgument;
    }

    @Override
    public List<Update<Double, AbstractInterval<R>>> monitor(
            Update<Double, V> signalUpdate)
    {
        List<Update<Double, AbstractInterval<R>>> updates = new ArrayList<>();

        List<Update<Double, AbstractInterval<R>>> firstArgUps =
                firstArgMonitor.monitor(signalUpdate);
        List<Update<Double, AbstractInterval<R>>> secondArgUps =
                secondArgMonitor.monitor(signalUpdate);


        SignalInterface<Double, AbstractInterval<R>> s1 = firstArgMonitor.getResult();
        SignalInterface<Double, AbstractInterval<R>> s2 = secondArgMonitor.getResult();

        //System.out.println(">>> Arg 1 updates: " + firstArgUps);

        for(Update<Double, AbstractInterval<R>> argU : firstArgUps) {
            List<Update<Double, AbstractInterval<R>>> ups =
                    BooleanComputation.binaryUp(s2, argU, opFunction);
            updates.addAll(ups);
            ups.forEach(rho::refine);
        }

        for(Update<Double, AbstractInterval<R>> argU: secondArgUps) {
            List<Update<Double, AbstractInterval<R>>> ups =
                    BooleanComputation.binaryUp(s1, argU, opFunction);
            updates.addAll(ups);
            ups.forEach(rho::refine);
        }

        System.out.println(">>> S1: " + s1);

        System.out.println(">>> S2: " + s2);

        System.out.println(">>> Binary Updates: " + updates);

        //for(Update<Double, AbstractInterval<R>> u: updates) {
        //    rho.refine(u);
        //}

        System.out.println(">>> Binary New Rho: " + rho);

        return updates;
    }

    @Override
    public SignalInterface<Double, AbstractInterval<R>> getResult() {
        return rho;
    }
}
