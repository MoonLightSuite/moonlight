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

package eu.quanticol.moonlight.online.monitoring;

import eu.quanticol.moonlight.online.signal.TimeChain;
import eu.quanticol.moonlight.core.signal.TimeSignal;
import eu.quanticol.moonlight.online.signal.Update;

import java.util.List;

/**
 * Primary interface for online monitoring.
 *
 * @param <T> The time domain of the monitoring process
 * @param <V> The domain of the signal being monitored
 * @param <R> Semantic Interpretation Semiring Type
 */
public interface OnlineMonitor<T extends Comparable<T>, V, R>
{
    /**
     * Execution starter of the monitoring process. It returns a list of updates
     * to the interpretation signal computed at the previous step.
     * @param signalUpdate update of the input signal
     * @return a list of updates to the interpretation signal
     */
    List<TimeChain<T, R>> monitor(Update<T, V> signalUpdate);
    //List<Update<T, R>> monitor(Update<T, V> signalUpdate);

    /**
     * Execution starter of the monitoring process. It returns a list of update
     * sequences to the interpretation signal computed at the previous step.
     * @param updates sequence of connected updates of the input signal
     * @return a list of updates to the interpretation signal
     */
    List<TimeChain<T, R>> monitor(TimeChain<T, V> updates);

    /**
     * Returns the result of the monitoring process.
     * Usually the {@link OnlineMonitor#monitor(Update)} must normally be
     * executed before calling this method.
     * @return the signal resulting from the monitoring process
     */
    TimeSignal<T, R> getResult();
}
