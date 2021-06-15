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

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A segment chain is a subtype of {@link LinkedList} providing some specific
 * features for {@link SegmentInterface}s, like checking temporal integrity
 * constraints, a custom iterator etc.
 *
 * <p> Two data integrity constraints must hold on the data structure:
 *    <ul>
 *        <li>
 *            <em>Monotonicity</em> invariant:
 *            <code>forall element:
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
 * TODO: they should be enforced by mutators and are trivially
 *       satisfied at the beginning, i.e. with no segments
 *
 *
 * @see SegmentInterface
 * @param <T> The time domain of interest, typically a {@link Number}
 * @param <V>
 */
public class TimeChain<T extends Comparable<T> & Serializable, V>
        implements Iterable<SegmentInterface<T,V>>
{
    /**
     * Internal representation of the chain
     */
    private final List<SegmentInterface<T, V>> list;

    /**
     * Last time instant of definition of the chain
     */
    protected final T end;

    /**
     * It defines a chain of time segments that ends at some time instant
     * @param end the time instant from which the segment chain is not defined.
     */
    public TimeChain(T end) {
        this.end = end;
        this.list = new ArrayList<>();
    }

    /**
     * WARNING: this interface assumes the programmer is taking responsibility
     *          about the Monotonicity of the ordered list of segments.
     *          (This means that if the List of segments has some
     *           wrongly-ordered segments, the TimeChain <em>MONOTONICITY</em>
     *           is violated, but it will go undetected)
     * @param segments chain of segments, passed as list
     * @param end      last time value of the new chain
     */
    public TimeChain(List<SegmentInterface<T,V>> segments, T end) {
        if(end.compareTo(segments.get(segments.size() - 1).getStart()) < 0)
            throw new IllegalArgumentException(ENDING_COND);


        this.end = end;
        this.list = new ArrayList<>(segments);
    }

    public TimeChain<T, V> copy() {
        return new TimeChain<>(list, end);
    }

    public List<SegmentInterface<T, V>> toList() {
        return list;
    }

    public int size() {
        return list.size();
    }

    public Stream<SegmentInterface<T, V>> stream() {
        return list.stream();
    }

    public TimeChain<T,V> subChain(int from, int to, T end) {
        List<SegmentInterface<T,V>> segments = list.subList(from, to);
        return new TimeChain<>(segments, end);
    }

    public SegmentInterface<T, V> getFirst() {
        return list.get(0);
    }

    public SegmentInterface<T, V> get(int index) {
        return list.get(index);
    }

    public SegmentInterface<T, V> getLast() {
        return list.get(list.size() - 1);
    }

    public DiffIterator<SegmentInterface<T, V>> diffIterator() {
        return diffIterator(0);
    }

    public DiffIterator<SegmentInterface<T, V>> diffIterator(int index) {
        if (index < 0 || index > list.size())
            throw new IndexOutOfBoundsException("Index: " + index);
        return new DiffListItr(index);
    }

    @NotNull
    @Override
    public Iterator<SegmentInterface<T, V>> iterator() {
        return list.iterator();
    }

    @Override
    public void forEach(Consumer<? super SegmentInterface<T, V>> action) {
        list.forEach(action);
    }

    @Override
    public Spliterator<SegmentInterface<T, V>> spliterator() {
        return list.spliterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeChain)) return false;
        TimeChain<?, ?> timeChain = (TimeChain<?, ?>) o;
        return list.equals(timeChain.list) && end.equals(timeChain.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list, end);
    }

    private class DiffListItr implements
            DiffIterator<SegmentInterface<T, V>>
    {
        private final ListIterator<SegmentInterface<T, V>> itr;
        private boolean changed;

        public DiffListItr(int index) {
            itr = list.listIterator(index);
            changed = false;
        }

        @Override
        public boolean hasChanged() {
            return changed;
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
                itr.next();         // the pointer to super.lastReturned
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
            if(itr.hasNext())
                return peekNext();
            else
                return other;
        }

        /**
         * Fail-safe method for fetching data from previous element (if exists).
         *
         * @param other default value in case of failure
         * @return the previous value if present, otherwise the default one.
         */
        @Override
        public SegmentInterface<T, V> tryPeekPrevious(
                SegmentInterface<T, V> other)
        {
            if(itr.hasPrevious())
                return peekPrevious();
            else
                return other;
        }

        // ---------------------------- MUTATORS ---------------------------- //
        @Override
        public void remove() {
            itr.remove();
            changed = true;
        }

        @Override
        public void set(SegmentInterface<T, V> e) {
            itr.set(e);
            changed = true;
        }

        @Override
        public void add(SegmentInterface<T, V> e) {
            itr.add(e);
            changed = true;
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

    public boolean add(SegmentInterface<T, V> e) {
        if(this.end.compareTo(e.getStart()) > 0) {
            if (!list.isEmpty() &&
                    list.get(list.size() - 1).getStart().compareTo(e.getStart()) > 0)
                throw new IllegalArgumentException(MONOTONICITY);
            return list.add(e);
        } else
            throw new IllegalArgumentException(ENDING_COND);
    }

    public T getEnd() {
        return end;
    }

    private static final String MONOTONICITY =
            "Violating monotonicity: The chain must be in monotonic time order";

    private static final String ENDING_COND =
            "Violating ending condition: The chain must either end " +
                    "after the last segment or after the previous ending";
}

