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

package eu.quanticol.moonlight.core.signal;

import eu.quanticol.moonlight.online.signal.*;

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
public interface TimeSignal<T extends Comparable<T> & Serializable, V> {
    /**
     * Performs an update of the internal representation of the signal,
     * given the data available in the update.
     *
     * @param u the new data available from new knowledge
     * @return <code>true</code> if the refinement actually updates the signal.
     *         <code>false</code> otherwise
     * @throws UnsupportedOperationException when not allowed by implementors
     */
    boolean refine(Update<T, V> u) throws UnsupportedOperationException;

    /**
     * Performs an update of the internal representation of the signal,
     * given the data available in the update.
     *
     * @param updates the new data available from new knowledge
     * @return <code>true</code> if the refinement actually updates the signal.
     *         <code>false</code> otherwise
     * @throws UnsupportedOperationException when not allowed by implementors
     */
    boolean refine(TimeChain<T, V> updates) throws UnsupportedOperationException;



    /**
     * Returns the internal chain of segments.
     *
     * @return the total chain of segments of the signal
     * @throws UnsupportedOperationException when not allowed by implementors
     */
    TimeChain<T, V> getSegments() throws UnsupportedOperationException;

    /**
     * @param time the time instant of interest in looking at the signal value
     * @return the signal value at the time instant passed.
     */
    default V getValueAt(T time) {
        ChainIterator<Sample<T, V>> itr = getSegments().chainIterator();
        Sample<T, V> current = null;

        while (itr.hasNext()) {
            current = itr.next();
            if (current.getStart() .compareTo(time) > 0) {
                // We went too far, we have to look at the previous element
                // So we have to move the iterator twice back
                // (as we are now looking backwards)
                itr.previous();
                return itr.previous().getValue();
            }
        }

        if(current != null) // Single-segment signal
            return current.getValue();
        else
            throw new UnsupportedOperationException("Empty signal provided");
    }

    /**
     * Temporal projection operation that selects a sub-part of the signal
     * delimited by the time instants provided by the input parameters.
     *
     * @param from beginning of the time frame of interest
     * @param to ending of the time frame of interest
     * @return the chain of segments of the signal delimited by the input
     * @throws UnsupportedOperationException when not allowed by implementors
     */
    TimeChain<T, V> select(T from, T to);

}
