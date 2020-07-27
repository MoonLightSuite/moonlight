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
 * @see OnlineTemporalMonitoring
 * @see TemporalMonitor
 */
public class OnlineMonitorAtomic<T, R> implements OnlineTemporalMonitor<T, R> {

    private final Function<T, R> atom;
    private final Interval horizon;
    private final List<Signal<R>> worklist;
    private final R undefined;

    /**
     *
     * @param atomicFunction
     * @param parentHorizon
     */
    public OnlineMonitorAtomic(Function<T, R> atomicFunction,
                               Interval parentHorizon,
                               R unknown)
    {
        atom = atomicFunction;
        horizon = parentHorizon;
        worklist = new ArrayList<>();
        undefined = unknown;
    }


    @Override
    public Signal<R> monitor(Signal<T> signal) {
        //if(horizon.contains(signalEnd) || worklist.isEmpty()) {
            //update result
            worklist.add(signal.applyHorizon(atom, undefined, horizon.getStart(), horizon.getEnd()));
        //}
        return worklist.get(worklist.size() - 1); //return last computed value
    }

    /**
     * @return the definition horizon of the formula
     */
    public Interval getHorizon() {
        return horizon;
    }

    //TODO: for debugging purposes mainly
    @Override
    public List<R> getWorklist() {
        List<R> lastValues = new ArrayList<>();
        for(Signal<R> item: worklist) {
            lastValues.add(item.valueAt(0));
        }

        return lastValues;
    }
}
