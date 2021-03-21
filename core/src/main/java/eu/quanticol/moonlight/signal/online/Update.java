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
public class Update<T extends Comparable<T>, V>  {

    private final T start;
    private final T end;
    private final V value;

    public Update(T start, T end, V value) {
        this.start = start;
        this.end = end;
        this.value = value;
    }

    public T getStart() {
        return start;
    }

    public T getEnd() {
        return end;
    }

    public V getValue() {
        return value;
    }
}
