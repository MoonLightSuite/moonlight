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
import java.util.*;

/**
 * A segment chain is a subtype of {@link LinkedList} providing some specific
 * features for {@link SegmentInterface}s, like checking temporal integrity
 * constraints, a custom iterator etc.
 *
 * <p> Two data integrity constraints must hold on the data structure:
 *    <ul>
 *        <li>
 *            <em>Monotonicity</em> invariant:
 *            <code>forall element():
 *                      current.getStart() < next.getStart() &&
 *                      current.getStart() > prev.getStart()
 *            </code>
 *        </li>
 *        <li>
 *            <em>Ending condition</em> invariant:
 *            <code>end > getLast().getStart() && old.end <= new.end</code>
 *        </li>
 *    </ul>
 *
 * <p>
 * TODO: they should be enforced by mutators and are is trivially
 *       satisfied at the beginning, i.e. with no segments
 * TODO: addAll, set, add, offer, offerFirst, offerLast, push, listIterator, diffIterator
 *
 *
 * @see SegmentInterface
 * @param <T> The time domain of interest, typically a {@link Number}
 * @param <V>
 */
public class SegmentChain
        <T extends Comparable<T> & Serializable, V extends Comparable<V>>
        extends LinkedList<SegmentInterface<T, V>>
{
    /**
     * Last time instant of definition of the signal
     */
    private T end;

    /**
     * It defines a chain of time segments that ends at some time instant
     * @param end the time instant from which the segment chain is not defined.
     */
    public SegmentChain(T end) {
        this.end = end;
    }

    private SegmentChain(List<SegmentInterface<T,V>> segments, T end) {
        this.end = end;
        // TODO: instead of addAll a more efficient solution could be to
        //       generate a SubChain in the same way SubLists are generated.
        this.addAll(segments);
    }

    public SegmentChain<T,V> subChain(int from, int to, T end) {
        //TODO: should check relation between 'end' and 'to'
        List<SegmentInterface<T,V>> segments = subList(from, to);
        return new SegmentChain<>(segments, end);
    }

    @Override
    public void addFirst(SegmentInterface<T, V> e) {
        if(getFirst().getStart().compareTo(e.getStart()) > 0)
            super.addFirst(e);
        else
            throw new IllegalArgumentException(MONOTONICITY);
    }

    @Override
    public void addLast(SegmentInterface<T, V> e) {
        if(this.end.compareTo(e.getStart()) > 0)
            super.addLast(e);
        else
            throw new IllegalArgumentException(ENDING_COND);
    }

    @Override
    public boolean add(SegmentInterface<T, V> e) {
        if(this.end.compareTo(e.getStart()) > 0)
            return super.add(e);
        else
            throw new IllegalArgumentException(ENDING_COND);
    }



    public void setEnd(T end) {
        if(getLast().getStart().compareTo(end) < 0 &&
                this.end.compareTo(end) <= 0)
            this.end = end;
        else
            throw new IllegalArgumentException(ENDING_COND);

    }


    public DiffIterator<SegmentInterface<T, V>> diffIterator() {
        return diffIterator(0);
    }

    public DiffIterator<SegmentInterface<T, V>> diffIterator(int index) {
        // To implicitly call checkPositionIndex(index) we call parent's method
        SegmentChain.super.listIterator(index);
        return new DiffListItr(index);
    }

    private class DiffListItr implements DiffIterator<SegmentInterface<T, V>> {
        private final ArrayList<SegmentInterface<T, V>>  changes;
        private final ListIterator<SegmentInterface<T, V>> itr;

        public DiffListItr(int index) {
            changes = new ArrayList<>();
            itr = SegmentChain.super.listIterator(index);
        }

        @Override
        public List<SegmentInterface<T, V>> getChanges() {
            return changes;
        }

        /**
         * @return the next element of the iterator by returning to
         *         the current position (or not moving at all)
         */
        @Override
        public SegmentInterface<T, V> peekNext() {
            if (itr.hasNext()) {
                SegmentInterface<T, V> e = itr.next();
                itr.previous();
                itr.previous();     // This repetition is done to also bring
                itr.next();         // the pointer to lastReturned
                return e;
            } else
                throw new NoSuchElementException("There is no next element!");
        }

        /**
         * @return the previous element of the iterator by returning to
         *         the current position (or not moving at all)
         */
        @Override
        public SegmentInterface<T, V> peekPrevious() {
            if (itr.hasPrevious()) {
                SegmentInterface<T, V> e = itr.previous();
                itr.next();
                return e;
            } else
                throw new NoSuchElementException("There is no previous " +
                                                                    "element!");
        }

        /**
         * Fail-safe method for fetching data from next element (if exists).
         *
         * @param other default value in case of failure
         * @return the next value if present, otherwise the default one.
         */
        @Override
        public SegmentInterface<T, V> tryPeekNext(SegmentInterface<T, V> other)
        {
            try {
                return peekNext();
            } catch (NoSuchElementException e) {
                return other;
            }
        }

        /**
         * Fail-safe method for fetching data from previous element (if exists).
         *
         * @param other default value in case of failure
         * @return the previous value if present, otherwise the default one.
         */
        @Override
        public SegmentInterface<T, V> tryPeekPrevious(
                SegmentInterface<T, V> other) {
            try {
                return peekPrevious();
            } catch (NoSuchElementException e) {
                return other;
            }
        }

        // ---------------------------- MUTATORS ---------------------------- //
        @Override
        public void remove() {
            itr.remove();
            //TODO: should we track removals?
        }

        @Override
        public void set(SegmentInterface<T, V> e) {
            changes.add(e);
            itr.set(e);
        }

        @Override
        public void add(SegmentInterface<T, V> e) {
            changes.add(e);
            itr.add(e);
        }

        // ------------------------- END OF MUTATORS ------------------------ //

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
        public SegmentInterface<T, V> next() {
            return itr.next();
        }

        @Override
        public SegmentInterface<T, V> previous() {
            return itr.previous();
        }

        @Override
        public int previousIndex() {
            return itr.previousIndex();
        }
    }

    public T getEnd() {
        return end;
    }

    /**
     * @return the hash code value for this list
     * @implSpec This implementation uses exactly the code that is used to define the
     * list hash function in the documentation for the {@link List#hashCode}
     * method.
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Compares the specified object with this list for equality.
     * If that is the case, it also checks that they end at the same value.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        if(super.equals(o)) {
            try{
                return ((SegmentChain<T, V>) o).getEnd() == end;
            } catch (ClassCastException e) {
                return false;
            }
        }
        return false;
    }

    private static final String MONOTONICITY =
            "Violating monotonicity: The chain must be in monotonic time order";

    private static final String ENDING_COND =
            "Violating ending condition: The chain must either end " +
                    "after the last segment or after the previous ending";
}
