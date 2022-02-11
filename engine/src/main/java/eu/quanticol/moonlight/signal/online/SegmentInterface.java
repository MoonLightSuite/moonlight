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

/**
 * The basic interface to represent time segments.
 * Note that for memory-efficiency reasons, the ending of the
 * segment is not present.
 *
 * @param <T> The time domain of interest, typically a {@link Number}
 * @param <V> The value domain of interest
 *
 * @see TimeChain for a data structure that exploits them
 */
public interface SegmentInterface <T extends Comparable<T>, V>
        extends Comparable<SegmentInterface<T, V>>
{
    /**
     * @return the value of the segment
     */
    V getValue();

    /**
     * @return the time instant at which the segment started
     */
    T getStart();

    /**
     * @return the time instant at which the segment ends
     * @throws UnsupportedOperationException when the implementation does not
     *         store the ending of the Segment.
     */
    T getEnd() throws UnsupportedOperationException;


    default int compareTo(SegmentInterface<T, V> segment) {
        return getStart().compareTo(segment.getStart());
    }

}
