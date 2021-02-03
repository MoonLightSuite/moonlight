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

package eu.quanticol.moonlight.monitoring.online;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.monitoring.temporal.online.OnlineTemporalMonitoring;
import eu.quanticol.moonlight.signal.online.OnlineSignal;
import eu.quanticol.moonlight.signal.Signal;

import java.util.function.Function;

/**
 * Strategy to interpret (online) an atomic predicate on the signal of interest.
 *
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see OnlineTemporalMonitoring
 * @see TemporalMonitor
 */
public class MonitorAtomic<T extends Comparable<T>, R extends Comparable<R>> {

    private final Function<T, R> atomicFunction;
    private final Interval horizon;
    private final OnlineSignal<R> rho;
    private final R unknown;
    private double signalEnd;


    /**
     * Prepares an atomic online (temporal) monitor.
     * @param atomicFunction The function evaluated by the atomic predicate
     * @param parentHorizon The temporal horizon of the parent formula
     * @param unknown The element of the domain that represents no information
     */
    public MonitorAtomic(Function<T, R> atomicFunction,
                         Interval parentHorizon,
                         R unknown)
    {
        this.atomicFunction = atomicFunction;
        this.horizon = parentHorizon;
        this.unknown = unknown;
        this.rho = new OnlineSignal<>(unknown, unknown); //TODO: change to min and max
    }

    public Signal<R> monitor(double start, double end, AbstractInterval<T> v) {
        // If the previous signal end falls within the formula horizon,
        // we must recompute the monitoring.
        // We store each monitoring result in a list.
        // Whether updated or not, we can just return the last computed result.
        if(horizon.contains(signalEnd) || signalEnd == 0) {
            //update result
            //rho.add(signal.applyHorizon(atomicFunction, unknown,
            //        horizon.getStart(), horizon.getEnd()));
        }

        //signalEnd = signal.end();
        //return rho.get(rho.size() - 1); //return last computed value
        return null;
    }
}
