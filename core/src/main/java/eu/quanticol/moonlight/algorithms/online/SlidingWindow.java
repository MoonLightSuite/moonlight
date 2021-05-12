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

import static java.lang.Math.min;
import static java.lang.Math.max;

public class SlidingWindow<R> {
    private final TimeChain<Double, R> arg;
    private final Interval h;
    private final BinaryOperator<R> op;
    private final double uEnd;
    private final double uStart;
    private final double wSize;
    private final Window<Node<Double, R>> w = new Window<>();
    private final List<Update<Double, R>> updates = new ArrayList<>();
    //private final BiFunction<R, BiFunction<R, BinaryOperator<R>, R>, Boolean> survive;

    public SlidingWindow(TimeChain<Double, R> arg, Update<Double, R> u,
                         Interval opHorizon, BinaryOperator<R> op)
    {
        this.arg = arg;
        this.op = op;
        h = opHorizon;
        wSize = h.getEnd() - h.getStart();

        // We define the resulting update horizon, cut at 0,
        // as updating before time 0 doesn't make any sense.
        uStart = u.getStart() - h.getEnd() > 0 ? u.getStart() - h.getEnd() : 0;
        uEnd = u.getEnd() - h.getStart() > 0 ? u.getEnd() - h.getStart() : 0;
    }

    public List<Update<Double, R>> run() {
        DiffIterator<SegmentInterface<Double, R>> itr = arg.diffIterator();

        while(itr.hasNext()) {
            add(itr.next());
        }

        // We add the last pieces of the window to the updates
        while(!w.isEmpty() && w.getFirst().getStart() < uEnd) {
            //updates.add(popUpdate(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
            Node<Double, R> node = w.removeFirst();
            addUpdate(node.getStart(), node.getValue());
        }

        return updates;
    }

    private void add(SegmentInterface<Double, R> curr) {
        if(!w.isEmpty() &&
                curr.getStart() - w.getFirst().getStart() > wSize)
        {
            shift(curr.getStart() - wSize);
        }
        doAdd(curr.getStart(), curr.getValue());
        //w.setEnd(curr.getStart());
    }

    private void shift(double t) {
        if(!w.isEmpty()) {
            Node<Double, R> first = w.removeFirst();

            while(!w.isEmpty() && w.getFirst().getStart() < t) {
                addUpdate(first.getStart(), first.getValue());
                Node<Double, R> next = w.removeFirst();
                if(next.getStart() > t) {
                    w.addFirst(new Node<>(t, first.getValue()));
                    w.addFirst(new Node<>(next.getStart(), next.getValue()));
                } else
                    first = next;
            }

            addUpdate(first.getStart(), first.getValue());
            w.addFirst(new Node<>(t, first.getValue()));
        }
    }

    private void addUpdate(double t, R v) {
        if(t < uEnd) {
            //if (w.isEmpty())
                updates.add(new Update<>(t, uEnd, v));
            //else
            //    updates.add(new Update<>(t, w.getFirst().getStart(), v));
        }
    }

    private void doAdd(Double t, R v) {
        if(w.isEmpty())
            w.addLast(new Node<>(t, v));
        else {
            Node<Double, R> last = w.removeLast();
            double t2 = last.getStart();
            R v2 = last.getValue();
            if(v.equals(v2))
                w.addLast(new Node<>(t2, v2));
            R v3 = op.apply(v, v2);
            if(v.equals(v3))
                doAdd(t2, v3);
            else if(!v2.equals(v3)) {
                doAdd(t2, v3);
                w.addLast(new Node<>(t, v));
            } else {
                w.addLast(new Node<>(t2, v2));
                w.addLast(new Node<>(t, v));
            }
        }
    }

    private void doSlide(DiffIterator<SegmentInterface<Double, R>> itr) {
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
            t = setStartingTime(t, currStart, currEnd);


            // we must skip segments starting before the update horizon of
            // the current t
            if(//t >= currEnd - h.getStart() ||
                    currEnd - h.getStart() < uStart)
                continue;


            // We are exceeding the window size, we must pop left sides
            // of the window and we can propagate the related updates
            while(!w.isEmpty() && t - w.getFirst().getStart() > wSize) {
                generateUpdate(t, currStart, curr.getValue());
            }

            // We push out the exceeding part of the window's first segment
            cutLeft(t, currStart);

            // We clear the monotonic edge, adding the current value at the end
            makeMonotonic(t, currStart, currEnd, curr.getValue());
        }
    }

    private double setStartingTime(double t, double currStart, double currEnd) {
        t = max(t, currEnd - h.getEnd());
        if(currStart - h.getStart() >= uStart) {
            t = min(t, currStart - h.getStart());
        }
        return t;
    }

    // We are exceeding the window size, we must pop left sides
    // of the window and we can propagate the related updates
    private void cutLeft(double t, double currStart) {
        while(!w.isEmpty() && currStart - h.getEnd() > w.getFirst().getStart())
        {
            Update<Double, R> oldFirst = popUpdate(t, currStart);

            if(currStart - h.getEnd() < oldFirst.getStart())
                oldFirst = new Update<>(oldFirst.getStart(),
                                   currStart - h.getEnd(),
                                        oldFirst.getValue());

            if(w.isEmpty() || oldFirst.getEnd() < w.getFirst().getStart())
                w.addFirst(new Node<>(oldFirst.getEnd(),
                                      oldFirst.getValue()));

            updates.add(oldFirst);
        }
    }

    /**
     * From the last to the first value of the window, we remove them until the
     * last element is strictly bigger than the current one
     */
    private void makeMonotonic(Double fromT,
                                          Double currStart, Double currEnd,
                                          R currV)
    {

        ListIterator<Node<Double, R>> itr = w.descendingIterator();
        Node<Double, R> last = w.isEmpty() ? null : w.getLast();
        Node<Double, R> next = null;
        while(itr.hasPrevious() && !isOptimum(last.getValue(), currV, op)
             )
        {
            last = itr.previous();
            if (isNotSurviving(last.getValue(), currV, op) || !itr.hasPrevious())
            {
                itr.remove();
//                if(next != null) {
//                    itr.next();
//                    itr.remove();
//                }
            } else if (!isNotSurviving(last.getValue(), currV, op)) {
                next = new Node<>(last.getStart(), op.apply(last.getValue(), currV));
                itr.set(next);
            }

            fromT = last.getStart();
            currV = op.apply(last.getValue(), currV);

        }

        if(last != null && last.getStart() + h.getEnd() > fromT &&
                isOptimum(last.getValue(), currV, op)
          )
        {
            if(currEnd - h.getEnd() < fromT) {
                fromT = max(
                        last.getStart() + h.getEnd()
                        , currEnd - h.getEnd()
                        );
            }
            else {
                fromT = //min(fromT,
                            min(
                                last.getStart() + h.getEnd()
                                , max(0, currStart - h.getStart())
                        //    )
                        );
            }
        }

        itr.add(new Node<>(fromT, currV));

        // ---------------------------------------------

/*
        // w.lastV < currV => currV is global maximum, replace lastV
        // else => currV local minimum starting from max(fromT,currT)
        while(!w.isEmpty() && !op.apply(w.getLast().getValue(), currV)
                .equals(w.getLast().getValue()))
        {
            Node<Double, R> oldNode = w.removeLast();
            fromT = oldNode.getStart();
            currV = op.apply(oldNode.getValue(), currV);
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
            if(currEnd - h.getEnd() < fromT) {
                fromT = max(
                        w.getLast().getStart() + h.getEnd()
                        //, min(fromT + wSize
                        , currEnd - h.getEnd()
                        //, max(0, min(currEnd - wSize, currStart - h.getStart()))
                        //, max(0, currStart - h.getStart()))
                )
                //, max(0, currT - h.getStart()))
                ;
            }
            else {
                fromT = min(//min(fromT,
                        w.getLast().getStart() + h.getEnd() //)
                        //, min(fromT + wSize
                        // min(max(0, currEnd - h.getEnd())
                        //, max(0, min(currEnd - wSize, currStart - h.getStart()))
                        , max(0, currStart - h.getStart()))
                //)
                //, max(0, currT - h.getStart()))
                ;
                //fromT = min(max(fromT, w.getLast().getStart() + wSize), currT - h.getStart());
                //fromT = currT;
            }
            //currV = op.apply(w.getLast().getValue(), currV);
        }

        // We can add to the window the pair (lastT, currV)
        w.addLast(new Node<>(fromT, currV));
*/
    }

    private static <R> boolean isNotSurviving(R oldV, R newV, BinaryOperator<R> f)
    {
        R result = f.apply(oldV, newV);
        return result.equals(oldV) // || result.equals(newV)
               ;
    }

    private static <R> boolean isOptimum(R oldV, R newV, BinaryOperator<R> f) {
        R result = f.apply(oldV, newV);
        return result.equals(oldV);
    }

    /**
     * if start > hStart && < hEnd, push update
     */
    private void generateUpdate(double outT, double currT, R currV) {
        Node<Double, R> f = w.removeFirst();
        R newV = f.getValue();
        double end = //min(
                min(uEnd, outT)
                //, currT - h.getEnd())
                ;
        if (!w.isEmpty())
            end = min(end, w.getFirst().getStart());

        if (currT - h.getEnd() < end && !op.apply(f.getValue(), currV).equals(f.getValue()))
            end = currT - h.getEnd();

        updates.add(new Update<>(f.getStart(), end, newV));


        if (currT - h.getEnd() == end) {
            Node<Double, R> n = new Node<>(end, op.apply(f.getValue(), currV));

            if (!w.isEmpty() && w.getFirst().getStart() <= end) {
                n = new Node<>(end, op.apply(currV, w.getFirst().getValue()));
            }

            w.addLast(n);
        }
    }

    private Update<Double, R> popUpdate(double outT, double currT)
    {
        Node<Double, R> f = w.removeFirst();
        double end = min(min(uEnd, outT), currT - h.getEnd());

        if(!w.isEmpty())
            end = min(end, w.getFirst().getStart());

        return new Update<>(f.getStart(), end, f.getValue());
    }

    private static class Window<E> {
        private final LinkedList<E> deque;

        public Window() {
            this.deque = new LinkedList<>();
        }

        public E removeFirst() {
            return deque.removeFirst();
        }

        public boolean isEmpty() {
            return deque.isEmpty();
        }

        public E getFirst() {
            return deque.getFirst();
        }

        public E getLast() {
            return deque.getLast();
        }

        public void addLast(E e) {
            deque.addLast(e);
        }

        public void addFirst(E e) {
            deque.addFirst(e);
        }

        public E removeLast() {
            return deque.removeLast();
        }

        public ListIterator<E> descendingIterator() {
            return deque.listIterator(deque.isEmpty() ? 0 : deque.size());
        }
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
