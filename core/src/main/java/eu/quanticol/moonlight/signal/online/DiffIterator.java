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

import java.util.*;

/**
 * Interface that extends the ListIterator interface to also retrieve
 * a list of changes that should be updated on mutators invocation.
 *
 * @see TimeChain#diffIterator()
 */
public interface DiffIterator<E> extends ListIterator<E> {

    /**
     * @return A list of changes generated from list mutators
     */
    List<E> getChanges();

    /**
     * @return the next element of the iterator by keeping current position
     * @throws NoSuchElementException when there is no such element
     */
    E peekNext() throws NoSuchElementException;

    /**
     * @return the previous element of the iterator by keeping current position
     * @throws NoSuchElementException when there is no such element
     */
    E peekPrevious() throws NoSuchElementException;

    /**
     * Fail-safe method for fetching data from next element (if exists).
     *
     * @param other default value in case of failure
     * @return the next value if present, otherwise the other one.
     */
    E tryPeekNext(E other);

    /**
     * Fail-safe method for fetching data from previous element (if exists).
     *
     * @param other default value in case of failure
     * @return the previous value if present, otherwise the other one.
     */
    E tryPeekPrevious(E other);
}

