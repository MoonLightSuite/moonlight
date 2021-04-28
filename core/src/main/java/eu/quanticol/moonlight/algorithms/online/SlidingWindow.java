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
    private final double uEnd;
    private final double uStart;
    private final Deque<Node<Double, R>> w = new ArrayDeque<>();
    private final List<Update<Double, R>> updates = new ArrayList<>();

    public SlidingWindow(TimeChain<Double, R> arg, Update<Double, R> u,
                         Interval opHorizon, BinaryOperator<R> op)
    {
        this.arg = arg;
        this.op = op;
        h = opHorizon;

        // We define the resulting update horizon, cut at 0,
        // as updating before time 0 doesn't make any sense.
        uStart = u.getStart() - h.getEnd() > 0 ? u.getStart() - h.getEnd() : 0;
        uEnd = u.getEnd() - h.getStart() > 0 ? u.getEnd() - h.getStart() : 0;
    }

    public List<Update<Double, R>>  slide() {
        DiffIterator<SegmentInterface<Double, R>> itr = arg.diffIterator();

        doSlide(itr);

        // We add the last pieces of the window to the updates
        while(!w.isEmpty() && w.getFirst().getStart() < uEnd) {
            updates.add(popUpdate(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        }

        return updates;
    }

    private void doSlide(DiffIterator<SegmentInterface<Double, R>> itr) {
        double wSize = h.getEnd() - h.getStart();
        //double t = 0.0;
        double t = uStart;

        // While there are segments and
        // the update horizon ends after the current time-point
        while(itr.hasNext() && t < uEnd) {
            SegmentInterface<Double, R> curr = itr.next();
            double currStart = curr.getStart();
            double currEnd = itr.tryPeekNext(curr).getStart();


            // We update the result time pointer:
            // t = min(max(0, next.start - h.end), |curr.start - h.start|)
            // i.e. we take n = `next.start - h.end` if it is non-negative
            //      and m = `curr.start - h.start` if it is not negative
            //      then t is the minimum between n and m
            t = max(t, currEnd - h.getEnd());
            if(//!w.isEmpty() &&
                    currStart - h.getStart() >= uStart) {
                t = min(t, currStart - h.getStart());
            }


            // we must skip segments starting before the update horizon of
            // the current t
            if(t >= currEnd - h.getStart() || currEnd - h.getStart() < uStart)
                continue;
            /*if(currEnd != currStart && t >= currEnd - h.getStart() ||
               (currEnd - h.getEnd() < hStart
                       //&& t + wSize < currEnd
                       //&& currStart - h.getStart() < hStart
               ))
                continue;*/


            // We are exceeding the window size, we must pop left sides
            // of the window and we can propagate the related updates
            while(!w.isEmpty() && t - w.getFirst().getStart() > wSize) {
                generateUpdate(t, currStart);
            }

            // We push out the exceeding part of the window's first segment
            cutLeft(t, currStart);

            // We clear the monotonic edge
            Node<Double, R> newNode = makeMonotonic(t, wSize, currStart, currEnd, curr.getValue());
            t = newNode.getStart();

            // We can add to the window the pair (lastT, currV)
            w.addLast(newNode);
        }
    }

    // We are exceeding the window size, we must pop left sides
    // of the window and we can propagate the related updates
    private void cutLeft(double t, double currStart) {
       // if(!w.isEmpty() && t - w.getFirst().getStart() > h.getEnd() - h.getStart()) {
        if(!w.isEmpty() && currStart - h.getEnd() > w.getFirst().getStart()) {
            Update<Double, R> oldFirst = popUpdate(t, currStart);
            w.addFirst(new Node<>(currStart - h.getEnd(),
                                  oldFirst.getValue()));
            if(currStart - h.getEnd() < oldFirst.getStart())
                oldFirst = new Update<>(oldFirst.getStart(), currStart - h.getEnd(), oldFirst.getValue());

            updates.add(oldFirst);
        }
    }

    /**
     * From the last to the first value of the window, we remove them until the
     * last element is strictly bigger than the current one
     */
    private Node<Double, R> makeMonotonic(Double fromT, Double wSize,
                                          Double currStart, Double currEnd,
                                          R currV)
    {
        // w.lastV < currV => currV is global maximum, replace lastV
        // else => currV local minimum starting from max(fromT,currT)
        while(!w.isEmpty() && !op.apply(w.getLast().getValue(), currV)
                                 .equals(w.getLast().getValue()))
        {
            fromT = w.getLast().getStart();
            currV = op.apply(w.getLast().getValue(), currV);
            w.removeLast();
        }

        // if   w.last.start + wSize > fromT &&
        //      w.last.value `op` currV == w.last.value &&
        //      w.last.start + wSize < currEnd - wSize
        // then the last value of the window is still valid for the current time instant,
        // and will be so until the current segment ends, i.e. currEnd - wSize
        if(!w.isEmpty() &&
           w.getLast().getStart() + h.getEnd() > fromT &&
           op.apply(w.getLast().getValue(), currV).equals(w.getLast().getValue()))
        {
            //double newT = w.getLast().getStart();
            if(currEnd - h.getEnd() <= fromT) {
                fromT = max(
                        w.getLast().getStart() + h.getEnd()
                        //, min(fromT + wSize
                         , max(0, currEnd - h.getEnd())
                        //, max(0, min(currEnd - wSize, currStart - h.getStart()))
                        //, max(0, currStart - h.getStart()))
                )
                //, max(0, currT - h.getStart()))
                ;
            }
            else
                fromT = min(
                        w.getLast().getStart() + h.getEnd()
                        //, min(fromT + wSize
                        // min(max(0, currEnd - h.getEnd())
                        //, max(0, min(currEnd - wSize, currStart - h.getStart()))
                        , max(0, currStart - h.getStart()))
                        //)
                        //, max(0, currT - h.getStart()))
                        ;
            /*if(fromT + h.getEnd() < currEnd)
                fromT = min(newT, currStart - h.getStart());
            else
                fromT = min(newT, fromT);*/
            //fromT = min(max(fromT, w.getLast().getStart() + wSize), currT - h.getStart());
            //fromT = currT;
        }

        return new Node<>(fromT, currV);
    }

    /**
     * if start > hStart && < hEnd, push update
     */
    private void generateUpdate(double outT, double currT) {
        Node<Double, R> f = w.removeFirst();
        //if(f.getStart() >= uStart) {
            double end = //min(
                    min(uEnd, outT)
                    //, currT - h.getEnd())
                    ;
            if(!w.isEmpty())
                end = min(end, w.getFirst().getStart());

            updates.add(new Update<>(f.getStart(), end, f.getValue()));
        //}
    }

    private Update<Double, R> popUpdate(double outT, double currT)
    {
        Node<Double, R> f = w.removeFirst();
        //Double end = hEnd;
        double end = min(min(uEnd, outT), currT - h.getEnd());
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
            return "<" + start.toString() + " , " + value.toString()+ ">";
        }
    }

}
