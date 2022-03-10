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

package eu.quanticol.moonlight.online.signal;

import eu.quanticol.moonlight.core.signal.Sample;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A segment chain is similar to a {@link LinkedList}, providing some
 * specific features for {@link Sample}s, like checking temporal
 * integrity constraints, a custom iterator etc.
 *
 * <p> Let <code>old</code> and <code>new</code> denote the state of
 * <code>this</code> before and after some mutating operations.
 * Two data integrity constraints must hold on the data structure:
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
 * TODO: they should be enforced by mutators and trivially
 *       satisfied at the beginning, i.e. with no segments
 *
 *
 * @see Sample
 * @param <T> The time domain of interest, typically a {@link Number}
 * @param <V>
 */
public class TimeChain<T extends Comparable<T> & Serializable, V>
        implements Iterable<Sample<T,V>>
{
    /**
     * Internal representation of the chain
     */
    private final List<Sample<T, V>> segments;

    /**
     * Last time instant of definition of the chain
     */
    private final T end;

    /**
     * It defines a chain of time segments that ends at some time instant
     * @param end the time instant from which the segment chain is not defined.
     */
    public TimeChain(@NotNull T end) {
        this.end = end;
        this.segments = new ArrayList<>();
    }

    /**
     * It defines a chain of time segments that ends at some time instant
     * @param end the time instant from which the segment chain is not defined.
     */
    public TimeChain(@NotNull Sample<T, V> element, @NotNull T end) {
        if(end.compareTo(element.getStart()) < 0)
            throw new IllegalArgumentException(ENDING_COND);
        
        this.end = end;
        segments = new ArrayList<>();
        segments.add(element);
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
    public TimeChain(@NotNull List<Sample<T,V>> segments,
                     @NotNull T end)
    {
        if(segments.isEmpty())
            throw new IllegalArgumentException("Invalid Segment list");
        if(end.compareTo(segments.get(segments.size() - 1).getStart()) < 0)
            throw new IllegalArgumentException(ENDING_COND);

        this.end = end;
        this.segments = new ArrayList<>(segments);
    }

    /**
     * Adds a segment to the TimeChain.
     * @param e segment to add
     * @return true as specified by {@link List#add(Object)}
     * @throws IllegalArgumentException when monotonicity
     *                                    or ending condition are violated.
     */
    public boolean add(Sample<T, V> e) {
        if(end.compareTo(e.getStart()) > 0) {
            checkMonotonicity(e.getStart());
            return segments.add(e);
        } else
            throw new IllegalArgumentException(ENDING_COND);
    }

    /**
     * Checks if monotonicity is violated at time t
     * @param t new time value to check
     */
    private void checkMonotonicity(T t) {
        if (!segments.isEmpty() &&
                segments.get(segments.size() - 1).getStart().compareTo(t) > 0)
            throw new IllegalArgumentException(MONOTONICITY);
    }

    /**
     * Shallow copy of the chain
     * @return a new TimeChain defined on the same data
     */
    public TimeChain<T, V> copy() {
        return new TimeChain<>(segments, end);
    }

    /**
     * Generates a sub-chain of the current chain.
     * <em>WARNING:</em> Similarly to {@link List#subList(int, int)},
     * the new chain shares the data structure with the current one, therefore
     * modifications will be reflected to both.
     *
     * @param from starting segment's index of the new chain
     *
     * @param to ending segment's index of the new chain
     * @param end ending time of the new chain
     * @return a new chain on current data, defined on the provided bounds
     */
    public TimeChain<T,V> subChain(int from, int to, T end) {
        List<Sample<T,V>> newList = this.segments.subList(from, to);
        return new TimeChain<>(newList, end);
    }

    public boolean isEmpty() {
        return segments.isEmpty();
    }

    public void clear() {
        segments.clear();
    }

    /**
     * Returns the last element of the chain
     * @return last element of the chain
     */
    public Sample<T, V> getLast() {
        return segments.get(segments.size() - 1);
    }

    public ChainIterator<Sample<T, V>> chainIterator() {
        return chainIterator(0);
    }

    public ChainIterator<Sample<T, V>> chainIterator(int index) {
        if (index < 0 || index > segments.size())
            throw new IndexOutOfBoundsException("Index: " + index);
        return new ChainIterator<>(segments, index);
    }

    public List<Update<T, V>> toUpdates() {
        List<Update<T, V>> updates = new ArrayList<>(segments.size());
        for(int i = 0; i < segments.size(); i++) {
            T uEnd = end;
            if(i != segments.size() - 1)
                uEnd = segments.get(i + 1).getStart();

            Update<T, V> u = new Update<>(segments.get(i).getStart(),
                                          uEnd,
                                          segments.get(i).getValue());
            updates.add(u);
        }
        return updates;
    }

    @NotNull
    @Override
    public Iterator<Sample<T, V>> iterator() {
        return segments.iterator();
    }

    @Override
    public void forEach(Consumer<? super Sample<T, V>> action) {
        segments.forEach(action);
    }

    @Override
    public Spliterator<Sample<T, V>> spliterator() {
        return segments.spliterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeChain)) return false;
        TimeChain<?, ?> timeChain = (TimeChain<?, ?>) o;
        return segments.equals(timeChain.segments) && end.equals(timeChain.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(segments, end);
    }

    public Stream<Sample<T, V>> stream() {
        return segments.stream();
    }

    public T getEnd() {
        return end;
    }

    public List<Sample<T, V>> toList() {
        return segments;
    }

    public int size() {
        return segments.size();
    }

    public Sample<T, V> getFirst() {
        return segments.get(0);
    }

    public T getStart() {
        return this.getFirst().getStart();
    }

    public Sample<T, V> get(int index) {
        return segments.get(index);
    }

    private static final String MONOTONICITY =
            "Violating monotonicity: The chain must be in monotonic time order";

    private static final String ENDING_COND =
            "Violating ending condition: The chain must either end " +
            "after the last segment or after the previous ending";

    @Override
    public String toString() {
        return "TimeChain{" +
                "segments=" + segments +
                ", end=" + end +
                '}';
    }

}

