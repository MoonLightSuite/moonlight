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
import eu.quanticol.moonlight.signal.online.*;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

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
    private final Window<R> w;
    private final Deque<Update<Double, R>> updates = new ArrayDeque<>();

    public SlidingWindow(TimeChain<Double, R> argument,
                         Update<Double, R> u,
                         Interval opHorizon,
                         BinaryOperator<R> op)
    {
        this(argument, u.getStart(), u.getEnd(), opHorizon, op);
    }

    public SlidingWindow(TimeChain<Double, R> argument,
                         TimeChain<Double, R> updates,
                         Interval opHorizon,
                         BinaryOperator<R> op)
    {
        this(argument, updates.getStart(), updates.getEnd(), opHorizon, op);
    }

    private SlidingWindow(TimeChain<Double, R> argument,
                          double start,
                          double end,
                          Interval opHorizon,
                          BinaryOperator<R> op)
    {
        this.op = op;
        arg = argument;
        h = opHorizon;
        w = new Window<>(0.0, opHorizon.getStart());
        uStart = setStartBoundary(start);
        uEnd = setEndBoundary(end);
    }

    /**
     * We define the resulting update horizon, cut at 0,
     * as updating before time 0 doesn't make any sense.
     * @param start starting time of the update
     * @return the starting value of the output, cut at 0
     */
    private double setStartBoundary(Double start) {
        return start - h.getEnd() > 0 ? start - h.getEnd() : 0;
    }

    /**
     * We define the resulting update horizon, cut at 0,
     * as updating before time 0 doesn't make any sense.
     * @param end starting time of the update
     * @return the ending value of the output, cut at 0
     */
    private double setEndBoundary(Double end) {
        return end - h.getStart() > 0 ? end - h.getStart() : 0;
    }

    /**
     * Primary entry point of the Sliding Window, it loops over the
     * input signal and adds eligible segments to the sliding window.
     * @return list of updates to the robustness signal
     */
    public List<TimeChain<Double, R>> runChain() {
        processArgument();
        List<TimeChain<Double, R>> chain = new ArrayList<>();
        chain.add(updatesToChain());
        return chain;
    }

    private void processArgument() {
        if(uEnd != 0.0) {
            arg.forEach(this::add);
            collectUpdates();
        }
    }

    /**
     * Primary entry point of the Sliding Window, it loops over the
     * input signal and adds eligible segments to the sliding window.
     * @return list of updates to the robustness signal
     */
    public List<Update<Double, R>> run() {
        processArgument();
        return new ArrayList<>(updates);
    }

    /**
     * Adds the last eligible pieces of the window to the updates,
     * and sets the updates right bound which was not set before
     */
    private void collectUpdates() {
        while(!w.isEmpty() && w.getStart() < uEnd) {
            Element<Double, R> element = w.removeFirst();
            addUpdate(element);
        }

        if(!updates.isEmpty()) {
            Update<Double, R> last = updates.removeLast();
            updates.add(new Update<>(last.getStart(), uEnd, last.getValue()));
        }
    }

    /**
     * <em>Temporary utility</em> Transforms a list of updates to a TimeChain
     * @return chained updates
     */
    private TimeChain<Double, R> updatesToChain() {
        List<SegmentInterface<Double, R>> ups =
                updates.stream()
                        .map(u -> new TimeSegment<>(u.getStart(), u.getValue()))
                        .collect(Collectors.toList());

        return new TimeChain<>(ups, updates.getLast().getEnd());
    }

    /**
     * This method adds the current segment of the input to the sliding window.
     * The addition operation might induce a shift in the sliding window and
     * might result in the removal of some previous elements.
     *
     * @param curr current segment of the input signal
     */
    private void add(SegmentInterface<Double, R> curr) {
        double newStart = curr.getStart() - h.getStart();
        double newEnd = curr.getStart() - h.getEnd();

        if(!w.isEmpty() && curr.getStart() - w.getStart() >  h.getEnd())
            slide(newEnd);
        else if(w.getEnd() - h.getStart() < newEnd)
            w.clear();

        doAdd(newStart, curr.getValue());
    }

    /**
     * Shifts the window when the size is exceeded, popping the left side
     * which will be delivered as updates.
     *
     * @param timeBound new time point entering the window
     */
    private void slide(double timeBound) {
        if(!w.isEmpty()) {
            Element<Double, R> first = keepSliding(timeBound);
            addUpdate(first);

            if(w.isEmpty() || w.getStart() > timeBound)
                w.addFirst(timeBound, first.getValue());
        }
    }

    /**
     * Slides the window until the time bound is reached,
     * returning the new first element of the window
     * @param timeBound time bound to which keeping sliding
     * @return the new first element of the window
     */
    private Element<Double, R> keepSliding(double timeBound) {
        Element<Double, R> first = w.removeFirst();
        while(!w.isEmpty() && w.getStart() < timeBound) {
            addUpdate(first);
            first = checkNext(timeBound, first);
        }
        return first;
    }

    private Element<Double, R> checkNext(double timeBound,
                                         Element<Double, R> prev)
    {
        Element<Double, R> next = w.removeFirst();
        if(next.getStart() > timeBound) {
            w.addFirst(timeBound, prev.getValue());
            w.addFirst(next.getStart(), next.getValue());
            return prev;
        } else
            return next;
    }

    /**
     * Saves the current element of the window for caller's update
     * @param element current element
     */
    private void addUpdate(Element<Double, R> element) {
        //TODO: updates management is highly inefficient, should be refactored
        double t = element.getStart();
        if(t < uEnd) {
            t = t < 0 ? 0 : t;
            updateLast(t);
            updates.add(new Update<>(t, Double.NaN, element.getValue()));
        }
    }

    /**
     * refreshes last saved update, making it end properly
     * @param newEnd new ending time of the last update
     */
    private void updateLast(double newEnd) {
        if(!updates.isEmpty()) {
            if(updates.getFirst().getStart() == newEnd)
                updates.clear();
            else {
                Update<Double, R> old = updates.removeLast();
                updates.add(new Update<>(old.getStart(), newEnd, old.getValue()));
            }
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
        Deque<Element<Double, R>> tail = new ArrayDeque<>();
        boolean completed = false;
        while(!w.isEmpty() && !completed) {
            Element<Double, R> last = w.removeLast();
            double t2 = last.getStart();
            R v2 = last.getValue();
            R v3 = op.apply(v, v2);
            if (v.equals(v2)) {
                w.addLast(t2, v2);
                completed = true;
            } else if(v.equals(v3)) {
                t = t2;
                v = v3;
            } else if(!v2.equals(v3)) {
                tail.addFirst(new Element<>(t, v));
                t = t2;
                v = v3;
            } else {
                w.addLast(t2, v2);
                w.addLast(t, v);
                completed = true;
            }
        }

        if(w.isEmpty())
            w.addLast(t, v);

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

        public Double getStart() {
            return deque.isEmpty() ? endingTime : deque.getFirst().getStart();
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

        public void addLast(double start, V value) {
            Element<Double, V> e = new Element<>(start, value);
            deque.addLast(e);
            endingTime = start + offset;
        }

        public void addFirst(double start, V value) {
            deque.addFirst(new Element<>(start, value));
        }

        public Element<Double, V> removeLast() {
            return deque.removeLast();
        }

        public void clear() {
            deque.clear();
        }

        public Double getEnd() {
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
