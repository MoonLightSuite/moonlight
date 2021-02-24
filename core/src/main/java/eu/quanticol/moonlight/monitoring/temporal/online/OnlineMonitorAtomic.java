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

package eu.quanticol.moonlight.monitoring.temporal.online;

import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.Signal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Strategy to interpret (online) an atomic predicate on the signal of interest.
 *
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see LegacyOnlineTemporalMonitoring
 * @see TemporalMonitor
 */
public class OnlineMonitorAtomic<T, R> implements TemporalMonitor<T, R> {

    private final Function<T, R> atomicFunction;
    private final Interval horizon;
    private final List<Signal<R>> worklist;
    private final R unknown;
    private double signalEnd;

    /**
     * Prepares an atomic online (temporal) monitor.
     * @param atomicFunction The function evaluated by the atomic predicate
     * @param parentHorizon The temporal horizon of the parent formula
     * @param unknown The element of the domain that represents no information
     */
    public OnlineMonitorAtomic(Function<T, R> atomicFunction,
                               Interval parentHorizon,
                               R unknown)
    {
        this.atomicFunction = atomicFunction;
        this.horizon = parentHorizon;
        this.unknown = unknown;
        this.worklist = new ArrayList<>();

        // By convention we assume that previous monitoring stopped at time 0
        this.signalEnd = 0;
    }


    @Override
    public Signal<R> monitor(Signal<T> signal) {
        // If the previous signal end falls within the formula horizon,
        // we must recompute the monitoring.
        // We store each monitoring result in a list.
        // Whether updated or not, we can just return the last computed result.
        if(horizon.contains(signalEnd) || signalEnd == 0) {
            //update result
            worklist.add(signal.applyHorizon(atomicFunction, unknown,
                                         horizon.getStart(), horizon.getEnd()));
        }

        signalEnd = signal.end();
        return worklist.get(worklist.size() - 1); //return last computed value
    }

    /**
     * @return the definition horizon of the formula
     */
    public Interval getHorizon() {
        return horizon;
    }
}
