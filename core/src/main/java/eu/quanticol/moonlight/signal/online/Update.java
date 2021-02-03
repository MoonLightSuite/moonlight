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
 *
 * @param <T>
 * @param <V>
 */
public class Update<T extends Comparable<T>, V extends Comparable<V>>  {

    private final T start;
    private final T end;
    private final V value;

    Update(T start, T end, V value) {
        this.start = start;
        this.end = end;
        this.value = value;
    }

    T getStart() {
        return start;
    }

    T getEnd() {
        return end;
    }

    V getValue() {
        return value;
    }

    /**
     * @return an update signal, given a signal and a function
     */
    /*
    public <R extends Comparable<R>> Update<AbstractInterval<R>> apply(
            Function<AbstractInterval<V>, AbstractInterval<R>> f,
            Update<AbstractInterval<V>> u)
    {
        AbstractInterval<R> v = f.apply(u.getValue());
        return new Update<>(u.getStart(), u.getEnd(), v);
    }*/

    /**
     * @return an update signal, given a signal and a function
     */
    /*
    public <R extends Comparable<R>> Update<AbstractInterval<R>> apply(
            Function<AbstractInterval<V>, AbstractInterval<R>> f,
            Update<AbstractInterval<V>> u1, Update<AbstractInterval<V>> u2)
    {
        AbstractInterval<R> v = f.apply(u1.getValue());
        return new Update<>(u1.getStart(), u1.getEnd(), v);
    }
    */

}
