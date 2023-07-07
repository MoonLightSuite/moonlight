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

package io.github.moonlightsuite.moonlight.online.signal;


import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Iterator that extends the ListIterator interface with some handy methods.
 *
 * TODO: Mutators should check the order is kept by using Comparable
 *
 * @see TimeChain#chainIterator()
 */
public class ChainIterator<E> implements ListIterator<E> {
    private final ListIterator<E> itr;
    private boolean changed;

    public ChainIterator(List<E> list, int index) {
        itr = list.listIterator(index);
        changed = false;
    }

    public ChainIterator(List<E> list) {
        this(list, 0);
    }

    /**
     * @return <code>true</code> if the list didn't mutate during iteration.
     *         <code>false</code> otherwise.
     */
    public boolean noEffects() {
        return !changed;
    }

    /**
     * @return the next element of the iterator by keeping current position
     * @throws NoSuchElementException when there is no such element
     */
    public E peekNext() {
        if (hasNext()) {
            E e = next();
            previous();
            if(hasPrevious()) {
                previous();     // This repetition is done to also bring
                next();         // the pointer to super.lastReturned
            }
            return e;
        } else
            throw new NoSuchElementException(NO_NEXT);
    }

    /**
     * @return the previous element of the iterator by keeping current position
     * @throws NoSuchElementException when there is no such element
     */
    public E peekPrevious() {
        if (hasPrevious()) {
            E e = previous();
            next();
            return e;
        } else
            throw new NoSuchElementException(NO_PREV);
    }

    /**
     * Fail-safe method for fetching data from next element (if exists).
     *
     * @param other default value in case of failure
     * @return the next value if present, otherwise the other one.
     */
    public E tryPeekNext(E other)
    {
        if(hasNext())
            return peekNext();
        else
            return other;
    }

    /**
     * Fail-safe method for fetching data from previous element (if exists).
     *
     * @param other default value in case of failure
     * @return the previous value if present, otherwise the other one.
     */
    public E tryPeekPrevious(E other)
    {
        if(hasPrevious())
            return peekPrevious();
        else
            return other;
    }

    @Override
    public boolean hasNext() {
        return itr.hasNext();
    }

    @Override
    public boolean hasPrevious() {
        return itr.hasPrevious();
    }

    @Override
    public int nextIndex() {
        return itr.nextIndex();
    }

    @Override
    public int previousIndex() {
        return itr.previousIndex();
    }

    @Override
    public E next() {
        return itr.next();
    }

    @Override
    public E previous() {
        return itr.previous();
    }

    // ---------------------------- MUTATORS ---------------------------- //
    @Override
    public void remove() {
        itr.remove();
        changed = true;
    }

    @Override
    public void set(E e) {
        itr.set(e);
        changed = true;
    }

    @Override
    public void add(E e) {
        itr.add(e);
        changed = true;
    }
    // ------------------------- END OF MUTATORS ------------------------ //

    private static final String NO_NEXT = "There is no next element!";
    private static final String NO_PREV = "There is no previous element!";
}

