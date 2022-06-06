/*******************************************************************************
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
 *******************************************************************************/
package eu.quanticol.moonlight.offline.signal;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * A <code>SignalCursor</code> is used to scan values in a signal.
 */
public interface SignalCursor<T, V> {

    static <C> boolean isNotCompleted(List<SignalCursor<Double, C>> cursors) {
        return isNotCompleted(cursors.stream());
    }

    private static <C> boolean isNotCompleted(Stream<SignalCursor<Double, C>> cursors) {
        return cursors.map(c -> !c.isCompleted())
                .reduce(true, (c1, c2) -> c1 && c2);
    }

    boolean isCompleted();

    @SafeVarargs
    static <C> boolean isNotCompleted(SignalCursor<Double, C>... cursors) {
        return isNotCompleted(Arrays.stream(cursors));
    }

    T getCurrentTime();

    V getCurrentValue();

    void forward();

    void backward();

    void revert();

    void move(T t);

    T nextTime();

    T previousTime();

    boolean hasNext();

    boolean hasPrevious();

}
