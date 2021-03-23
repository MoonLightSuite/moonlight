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

package eu.quanticol.moonlight.signal.online;

import java.io.Serializable;

/**
 * General interface that represents a Signal used by the monitoring processes
 *
 * @param <T> The time domain of interest, typically a {@link Number}
 * @param <V> The signal domain to be considered
 *
 * @see OnlineSignal for a concrete implementation
 * @see MultiOnlineSignal for a concrete implementation
 */
public interface SignalInterface<T extends Comparable<T> & Serializable, V>
{

    /**
     * @param time the time instant of interest for looking at the signal value
     * @return the signal value at the time instant passed.
     */
    V getValueAt(T time);

    /**
     * Performs an update of the internal representation of the signal,
     * given the data available in the update.
     *
     * @param u the new data available from new knowledge
     * @return <code>true</code> if the refinement actually updates the signal.
     *         <code>false</code> otherwise
     */
    boolean refine(Update<T, V> u);

    /**
     * Temporal projection operation that selects a sub-part of the signal
     * delimited by the time instants provided by the input parameters.
     *
     * @param start beginning of the time frame of interest
     * @param end ending of the time frame of interest
     * @return the chain of segments of the signal delimited by the input
     */
    SegmentChain<T, V> select(T start, T end);
}
