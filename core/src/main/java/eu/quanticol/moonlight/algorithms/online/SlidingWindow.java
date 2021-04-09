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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.BinaryOperator;

import static java.lang.Math.min;
import static java.lang.Math.max;
import static java.lang.Math.abs;

public class SlidingWindow<R> {
    private final TimeChain<Double, R> arg;
    private final Interval h;
    private final BinaryOperator<R> op;
    private final double hEnd;
    private final double hStart;
    private final Deque<Node<Double, R>> w = new ArrayDeque<>();
    private final List<Update<Double, R>> updates = new ArrayList<>();
    private final Update<Double, R> u;

    public SlidingWindow(TimeChain<Double, R> arg, Update<Double, R> update,
                         Interval opHorizon, BinaryOperator<R> op)
    {
        this.arg = arg;
        h = opHorizon;
        hEnd = update.getEnd() - opHorizon.getStart();
        hStart = update.getStart() - h.getEnd();
        this.op = op;
        u = update;
    }

    public List<Update<Double, R>>  slide() {
        DiffIterator<SegmentInterface<Double, R>> itr = arg.diffIterator();

        doSlide(itr);

        // We add the last pieces of the window to the updates
        while(!w.isEmpty() && w.getFirst().getStart() < hEnd) {
            updates.add(popUpdate(Double.POSITIVE_INFINITY));
        }

        return updates;
    }

    private void doSlide(DiffIterator<SegmentInterface<Double, R>> itr) {
        double wSize = h.getEnd() - h.getStart();
        double t = 0.0;

        // While there are segments and
        // the update horizon ends after the current time-point
        while(itr.hasNext() && t < hEnd) {
            SegmentInterface<Double, R> curr = itr.next();
            SegmentInterface<Double, R> next = itr.tryPeekNext(curr);

            // We update the result time pointer:
            // t = min(max(0, next.start - h.end), |curr.start - h.start|)
            // i.e. we take n = `next.start - h.end` if it is non-negative
            //      and m = `curr.start - h.start` if it is not negative
            //      then t is the minimum between n and m
            double lastT = t;
            t = max(0, next.getStart() - h.getEnd());
            if(!w.isEmpty()) {
                t = min(t, abs(curr.getStart() - h.getStart()));
            }


            // `t >= next.start - op.horizon.start` or
            // `next.start - op.horizon.end < hStart`:
            //   We can skip this segment
            if(next != curr && t >= next.getStart() - h.getStart() ||
               next.getStart() - h.getEnd() < hStart)
                continue;

            // We are exceeding the window size, we must pop left sides
            // of the window and we can propagate the related updates
            while(!w.isEmpty() && t - w.getFirst().getStart() > wSize) {
                updates.add(popUpdate(t));
            }

            // We push out the exceeding part of the window's first segment
            cutLeft(t, curr.getStart());

            // We clear the monotonic edge
            Node<Double, R> newNode = makeMonotonic(t, wSize, curr.getStart(), curr.getValue());
            t = newNode.getStart();

            // We can add to the window the pair (lastT, currV)
            w.addLast(newNode);

            //System.out.println("The window has been updated: " + w.toString());
        }
    }

    private void cutLeft(double t, double currT) {
        if(!w.isEmpty() && currT - h.getEnd() > w.getFirst().getStart()) {
            Update<Double, R> oldFirst = popUpdate(t);
            updates.add(oldFirst);
            w.addFirst(new Node<>(currT - h.getEnd(),
                                  oldFirst.getValue()));
            //System.out.println("The window has been updated: " + w.toString());
        }
    }

    /**
     * From the last to the first value of the window, we remove them until the
     * last element is strictly bigger than the current one
     * @param fromT
     * @param currV
     * @return
     */
    private Node<Double, R> makeMonotonic(Double fromT, Double wSize, Double currT, R currV) {
        // w.lastV < currV => currV is global maximum, replace lastV
        // else => currV local minimum starting from max(fromT,currT)
        while(!w.isEmpty() && !op.apply(w.getLast().getValue(), currV)
                                 .equals(w.getLast().getValue()))
        {
            fromT = w.getLast().getStart();
            currV = op.apply(w.getLast().getValue(), currV);
            w.removeLast();
            //System.out.println("The window has been updated: " + w.toString());
        }

        if(!w.isEmpty() &&
           w.getLast().getStart() + wSize > fromT &&
           op.apply(w.getLast().getValue(), currV).equals(w.getLast().getValue()))
        {
            fromT = min(max(fromT, w.getLast().getStart() + wSize), currT - h.getStart());
            //fromT = currT;
        }

        return new Node<>(fromT, currV);
    }

    private Update<Double, R> popUpdate(double outT)
    {
        Node<Double, R> f = w.removeFirst();
        //System.out.println("The window has been updated: " + w.toString());
        //Double end = hEnd;
        Double end = min(hEnd, outT);
        if(!w.isEmpty())
            end = min(end, w.getFirst().getStart());

        return new Update<>(f.getStart(), end, f.getValue());
    }

    private static class Node<T, V> {
        private T start;
        private V value;

        public Node(T start, V value) {
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
            return "<" + start.toString() + " , " + value.toString();
        }
    }

}
