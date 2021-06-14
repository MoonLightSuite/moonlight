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

package eu.quanticol.moonlight.algorithms.online;

import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.signal.online.DiffIterator;
import eu.quanticol.moonlight.signal.online.TimeChain;
import eu.quanticol.moonlight.signal.online.SegmentInterface;
import eu.quanticol.moonlight.signal.online.Update;

import java.util.*;
import java.util.function.BinaryOperator;

/**
 *
 * @param <R> signal domain used for formula evaluation.
 */
public class SlidingWindow<R> {
    private final TimeChain<Double, R> arg;
    private final Interval h;
    private final BinaryOperator<R> op;
    private final double uEnd;
    private final double uStart;
    private final double wSize;
    private final Window<R> w;
    private final Deque<Update<Double, R>> updates = new ArrayDeque<>();

    public SlidingWindow(TimeChain<Double, R> arg, Update<Double, R> u,
                         Interval opHorizon, BinaryOperator<R> op)
    {
        this.arg = arg;
        this.op = op;
        h = opHorizon;
        wSize = h.getEnd() - h.getStart();
        w = new Window<>(0.0, h.getStart());

        // We define the resulting update horizon, cut at 0,
        // as updating before time 0 doesn't make any sense.
        uStart = u.getStart() - h.getEnd() > 0 ? u.getStart() - h.getEnd() : 0;
        uEnd = u.getEnd() - h.getStart() > 0 ? u.getEnd() - h.getStart() : 0;
    }

    /**
     * Primary entry point of the Sliding Window, it loops over the
     * input signal and adds eligible segments to the sliding window.
     * @return list of updates to the robustness signal
     */
    public List<Update<Double, R>> run() {
        if(uEnd != 0.0) {
            DiffIterator<SegmentInterface<Double, R>> itr = arg.diffIterator();

            while(itr.hasNext()) {
                add(itr.next());
            }

            collectUpdates();
        }
        return new ArrayList<>(updates);
    }

    /**
     * Adds the last eligible pieces of the window to the updates,
     * and sets the updates right bound which was not set before
     */
    private void collectUpdates() {
        while(!w.isEmpty() && w.getFirst().getStart() < uEnd) {
            Element<Double, R> element = w.removeFirst();
            addUpdate(element.getStart(), element.getValue());
        }

        if(!updates.isEmpty()) {
            Update<Double, R> last = updates.removeLast();
            updates.add(new Update<>(last.getStart(), uEnd, last.getValue()));
        }
    }

    /**
     * This method adds the current segment of the input to the sliding window.
     * The addition operation might induce a shift in the sliding window and
     * might result in the removal of some previous elements.
     *
     * @param curr current segment of the input signal
     */
    private void add(SegmentInterface<Double, R> curr) {
        if(!w.isEmpty() &&
              curr.getStart() - h.getStart() - w.getFirst().getStart() > wSize)
        {
            slide(curr.getStart() - h.getEnd());
        } else if(w.getEndingTime() + wSize < curr.getStart()) {
            w.clear();
        }
        doAdd2(curr.getStart() - h.getStart(), curr.getValue());
    }

    /**
     * Shifts the window when the size is exceeded, popping the left side
     * which will be delivered as updates.
     *
     * @param t new time point entering the window
     */
    private void slide(double t) {
        if(!w.isEmpty()) {
            Element<Double, R> first = w.removeFirst();

            while(!w.isEmpty() && w.getFirst().getStart() < t) {
                addUpdate(first.getStart(), first.getValue());
                Element<Double, R> next = w.removeFirst();
                if(next.getStart() > t) {
                    w.addFirst(new Element<>(t, first.getValue()));
                    w.addFirst(new Element<>(next.getStart(), next.getValue()));
                } else
                    first = next;
            }

            addUpdate(first.getStart(), first.getValue());
            w.addFirst(new Element<>(t, first.getValue()));
        }
    }

    /**
     * Adds the values of the current segment to the window.
     *
     * @param t starting time of the current segment
     * @param v value of the current segment
     */
    private void addUpdate(double t, R v) {
        if(t < uEnd) {
            t = t < 0 ? 0 : t;

            if(!updates.isEmpty()) {
                if(updates.getFirst().getStart() == t)
                    updates.clear();
                else {
                    Update<Double, R> old = updates.removeLast();
                    updates.add(new Update<>(old.getStart(), t, old.getValue()));
                }
            }

            updates.add(new Update<>(t, Double.NaN, v));
        }
    }

    /**
     * Actual adding logic of the algorithm.
     * It pops the right side of the window, until the last element dominates
     * the current value, then adds it to the end of the window, starting at
     * a time that is the minimum of the removed ones.
     *
     * @param t starting time of the current segment
     * @param v value of the current segment
     */
    private void doAdd(Double t, R v) {
        if(w.isEmpty())
            w.addLast(new Element<>(t, v));
        else {
            Element<Double, R> last = w.removeLast();
            double t2 = last.getStart();
            R v2 = last.getValue();
            R v3 = op.apply(v, v2);
            if (v.equals(v2)){
                w.addLast(new Element<>(t2, v2));
            } else if(v.equals(v3)) {
                doAdd(t2, v3);
            } else if(!v2.equals(v3)) {
                doAdd(t2, v3);
                w.addLast(new Element<>(t, v));
            } else {
                w.addLast(new Element<>(t2, v2));
                w.addLast(new Element<>(t, v));
            }
        }
    }

    private void doAdd2(Double t, R v) {
        ArrayDeque<Element<Double, R>> tail = new ArrayDeque<>();
        boolean completed = false;
        while(!w.isEmpty() && !completed) {
            Element<Double, R> last = w.removeLast();
            double t2 = last.getStart();
            R v2 = last.getValue();
            R v3 = op.apply(v, v2);
            if (v.equals(v2)) {
                w.addLast(new Element<>(t2, v2));
                completed = true;
            } else if(v.equals(v3)) {
                //doAdds(t2, v3);
                t = t2;
                v = v3;
            } else if(!v2.equals(v3)) {
                tail.addFirst(new Element<>(t, v));
                t = t2;
                v = v3;
                //doAdds(t2, v3);
            } else {
                w.addLast(new Element<>(t2, v2));
                w.addLast(new Element<>(t, v));
                completed = true;
            }
        }

        if(w.isEmpty())
            w.addLast(new Element<>(t, v));

        w.addAll(tail);
    }

    /**
     * Internal data structure used to keep the stored values and the starting
     * time of the last segment processed.
     *
     * @param <V> Type of the value of a window element
     */
    static class Window<V> {
        private final Deque<Element<Double, V>> deque;
        private double endingTime;
        private final double offset;

        public Window(Double startingTime, Double startingOffset) {
            endingTime = startingTime;
            offset = startingOffset;
            deque = new ArrayDeque<>();
        }

        public Element<Double, V> removeFirst() {
            return deque.removeFirst();
        }

        public boolean isEmpty() {
            return deque.isEmpty();
        }

        public int size() { return deque.size(); }

        public Element<Double, V> getFirst() {
            return deque.getFirst();
        }

        public Element<Double, V> getLast() {
            return deque.getLast();
        }

        public void addAll(Collection<Element<Double, V>> c) {
            deque.addAll(c);
        }

        public void addLast(Element<Double, V> e) {
            deque.addLast(e);
            endingTime = e.getStart() + offset;
        }

        public void addFirst(Element<Double, V> e) {
            deque.addFirst(e);
        }

        public Element<Double, V> removeLast() {
            return deque.removeLast();
        }

        public Iterator<Element<Double, V>> descendingIterator() {
            return deque.descendingIterator();
        }

        public void clear() {
            deque.clear();
        }

        public Double getEndingTime() {
            return endingTime;
        }
    }

    private static class Element<T, V> {
        private T start;
        private V value;

        public Element(T start, V value) {
            this.start = start;
            this.value = value;
        }

        public T getStart() {
            return start;
        }

        public V getValue() {
            return value;
        }

        public void setStart(T start) {
            this.start = start;
        }

        public void setValue(V value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "<" + start.toString() + " , " + value.toString()+ ">";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Element)) return false;
            Element<?, ?> element = (Element<?, ?>) o;
            return Objects.equals(start, element.start) &&
                    Objects.equals(value, element.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, value);
        }
    }

}
