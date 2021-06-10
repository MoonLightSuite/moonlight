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

package eu.quanticol.moonlight.signal;

import eu.quanticol.moonlight.signal.online.DiffIterator;
import eu.quanticol.moonlight.signal.online.SegmentInterface;
import org.jetbrains.annotations.NotNull;

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
 * TODO: addAll, add, set, push, offer, offerFirst, offerLast, listIterator, diffIterator
 *
 *
 * @see SegmentInterface
 * @param <T> The time domain of interest, typically a {@link Number}
 * @param <V>
 */
public class OldTimeChain
        <T extends Comparable<T> & Serializable, V>
        extends LinkedList<SegmentInterface<T, V>> implements Serializable
{

    private static final long serialVersionUID = -2785336975515274864L;
    /**
     * Last time instant of definition of the chain
     */
    protected T end;

    /**
     * It defines a chain of time segments that ends at some time instant
     * @param end the time instant from which the segment chain is not defined.
     */
    public OldTimeChain(T end) {
        this.end = end;
    }

    private OldTimeChain(List<SegmentInterface<T,V>> segments, T end) {
        this.end = end;
        // TODO: instead of addAll a more efficient solution could be to
        //       generate a SubChain in the same way SubLists are generated.
        this.addAll(segments);
    }

    public OldTimeChain<T, V> replicate() {
        //TODO: this method should be removed when TimeChain will be refactored
        // to encapsulate LinkedList instead of extending it
        Iterator<SegmentInterface<T, V>> itr = this.iterator();
        List<SegmentInterface<T, V>> newL = new ArrayList<>();

        while(itr.hasNext())
            newL.add(itr.next());

        return new OldTimeChain<>(newL, end);
    }

    @SuppressWarnings("java:S1185")
    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        super.removeRange(fromIndex, toIndex);
    }

    public boolean hasChanged() {
        return modCount > 0;
    }

    protected int getModCount() {
        return modCount;
    }

    public OldTimeChain<T,V> subChain(int from, int to, T end) {
        if(end.compareTo(get(to - 1).getStart()) < 0)
            throw new IllegalArgumentException(ENDING_COND);

        //TODO: commented out, work in progress
        //return new SubChain<>(this, from, to, end);

        List<SegmentInterface<T,V>> segments = subList(from, to);
        return new OldTimeChain<>(segments, end);
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
        OldTimeChain.super.listIterator(index);
        return new DiffListItr(index);
    }

    private class DiffListItr implements
            DiffIterator<SegmentInterface<T, V>>
    {
        private final ListIterator<SegmentInterface<T, V>> itr;
        private boolean changed;

        public DiffListItr(int index) {
            itr = OldTimeChain.super.listIterator(index);
            changed = false;
        }
        /**
         * @return A list of changes generated from list mutators
         */
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

    @Override
    public void push(SegmentInterface<T, V> e) {
        addFirst(e);
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

    public T getEnd() {
        return end;
    }

    private static final String MONOTONICITY =
            "Violating monotonicity: The chain must be in monotonic time order";

    private static final String ENDING_COND =
            "Violating ending condition: The chain must either end " +
                    "after the last segment or after the previous ending";
}

/**
 * {@inheritDoc}
 *
 * <p>This implementation returns a list that subclasses
 * {@code TimeChain}.  The subclass stores, in private fields, the
 * offset of the subChain within the backing list, the size of the subChain
 * (which can change over its lifetime), and the expected
 * {@code modCount} value of the backing list.
 *
 * <p>The subclass's {@code set(int, E)}, {@code get(int)},
 * {@code add(int, E)}, {@code remove(int)}, {@code addAll(int,
 * Collection)} and {@code removeRange(int, int)} methods all
 * delegate to the corresponding methods on the backing time chain,
 * after bounds-checking the index and adjusting for the offset.  The
 * {@code addAll(Collection c)} method merely returns {@code addAll(size,
 * c)}.
 *
 * <p>The {@code listIterator(int)} method returns a "wrapper object"
 * over a list iterator on the backing list, which is created with the
 * corresponding method on the backing list.  The {@code iterator} method
 * merely returns {@code listIterator()}, and the {@code size} method
 * merely returns the subclass's {@code size} field.
 *
 * <p>All methods first check to see if the actual {@code modCount} of
 * the backing list is equal to its expected value, and throw a
 * {@code ConcurrentModificationException} if it is not.
 *
 */
@SuppressWarnings("java:S110")
class OldSubChain<T extends Comparable<T> & Serializable, V>
        extends OldTimeChain<T, V> implements Serializable
{
    private static final long serialVersionUID = 815690343193147073L;

    private final OldTimeChain<T, V> l;
    private final int offset;
    private int subSize;

    OldSubChain(OldTimeChain<T, V> list, int fromIndex, int toIndex, T end) {
        super(end);

        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > list.size())
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                    ") > toIndex(" + toIndex + ")");
        l = list;
        offset = fromIndex;
        subSize = toIndex - fromIndex;
        this.modCount = l.getModCount();
    }

    @Override
    public SegmentInterface<T, V> set(int index, SegmentInterface<T, V> element)
    {
        rangeCheck(index);
        checkForComodification();
        return l.set(index + offset, element);
    }

    @Override
    public SegmentInterface<T, V> get(int index) {
        rangeCheck(index);
        checkForComodification();
        return l.get(index + offset);
    }

    @Override
    public int size() {
        checkForComodification();
        return subSize;
    }

    @Override
    public void add(int index, SegmentInterface<T, V> element) {
        rangeCheckForAdd(index);
        checkForComodification();
        l.add(index + offset, element);
        this.modCount = l.getModCount();
        subSize++;
    }

    @Override
    public SegmentInterface<T, V> remove(int index) {
        rangeCheck(index);
        checkForComodification();
        SegmentInterface<T, V> result = l.remove(index + offset);
        this.modCount = l.getModCount();
        subSize--;
        return result;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        checkForComodification();
        l.removeRange(fromIndex + offset, toIndex+offset);
        this.modCount = l.getModCount();
        subSize -= (toIndex-fromIndex);
    }

    @Override
    public boolean addAll(Collection<? extends SegmentInterface<T, V>> c) {
        return addAll(subSize, c);
    }

    @Override
    public boolean addAll(int index,
                          Collection<? extends SegmentInterface<T, V>> c)
    {
        rangeCheckForAdd(index);
        int cSize = c.size();
        if (cSize==0)
            return false;

        checkForComodification();
        l.addAll(offset + index, c);
        this.modCount = l.getModCount();
        subSize += cSize;
        return true;
    }

    @Override
    public @NotNull Iterator<SegmentInterface<T, V>> iterator() {
        return listIterator();
    }

    @Override
    public @NotNull ListIterator<SegmentInterface<T, V>>
    listIterator(final int index)
    {
        checkForComodification();
        rangeCheckForAdd(index);

        return new ListIterator<SegmentInterface<T, V>>() {
            private final ListIterator<SegmentInterface<T, V>> i =
                                        l.listIterator(index + offset);

            public boolean hasNext() {
                return nextIndex() < subSize;
            }

            public SegmentInterface<T, V> next() {
                if (hasNext())
                    return i.next();
                else
                    throw new NoSuchElementException();
            }

            public boolean hasPrevious() {
                return previousIndex() >= 0;
            }

            public SegmentInterface<T, V> previous() {
                if (hasPrevious())
                    return i.previous();
                else
                    throw new NoSuchElementException();
            }

            public int nextIndex() {
                return i.nextIndex() - offset;
            }

            public int previousIndex() {
                return i.previousIndex() - offset;
            }

            public void remove() {
                i.remove();
                OldSubChain.this.modCount = l.getModCount();
                subSize--;
            }

            public void set(SegmentInterface<T, V> e) {
                i.set(e);
            }

            public void add(SegmentInterface<T, V> e) {
                i.add(e);
                OldSubChain.this.modCount = l.getModCount();
                subSize++;
            }
        };
    }

    public @NotNull List<SegmentInterface<T, V>>
    subList(int fromIndex, int toIndex)
    {
        return new OldSubChain<>(this, fromIndex, toIndex, this.end);
    }

    private void rangeCheck(int index) {
        if (index < 0 || index >= subSize)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > subSize)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+ subSize;
    }

    private void checkForComodification() {
        if (this.modCount != l.getModCount())
            throw new ConcurrentModificationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OldSubChain)) return false;
        if (!super.equals(o)) return false;
        OldSubChain<?, ?> subChain = (OldSubChain<?, ?>) o;
        return offset == subChain.offset &&
                subSize == subChain.subSize &&
                l.equals(subChain.l);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), l, offset, subSize);
    }
}

