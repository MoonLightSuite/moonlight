package eu.quanticol.moonlight.algorithms.online;

import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.signal.online.DiffIterator;
import eu.quanticol.moonlight.signal.online.SegmentChain;
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

public class SlidingWindow<R extends Comparable<R>> {
    private final SegmentChain<Double, R> s;
    private final Interval h;
    private final BinaryOperator<R> op;
    private final double hEnd;
    private final double hStart;
    private final Update<Double, R> u;
    private final Deque<Node<Double, R>> w = new ArrayDeque<>();
    private final List<Update<Double, R>> updates = new ArrayList<>();

    public SlidingWindow(SegmentChain<Double, R> s, Update<Double, R> update,
                         Interval opHorizon, BinaryOperator<R> op)
    {
        this.s = s;
        h = opHorizon;
        hEnd = update.getEnd() - opHorizon.getStart();
        u = update;
        hStart = update.getStart() - h.getEnd();
        this.op = op;
    }

    public List<Update<Double, R>>  slide() {
        DiffIterator<SegmentInterface<Double, R>> itr = s.diffIterator();

        doSlide(itr);

        // We add the last pieces of the window to the updates
        while(!w.isEmpty()) {
            updates.add(pushUpdate());
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
            t = max(0, next.getStart() - h.getEnd());
            if(!w.isEmpty())
                t = min(t, abs(curr.getStart() - h.getStart()));


            // t >= next.start - op.horizon.start:
            //   We can skip this segment safely
            if(t >= next.getStart() - h.getStart()
               ||
               next.getStart() - h.getEnd() < hStart
              )
                continue;

            // We are exceeding the window size, we must pop left sides
            // of the window and we can propagate the related updates
            while(!w.isEmpty() && t - w.getFirst().getStart() > wSize) {
                updates.add(pushUpdate());
            }

            // We push out the exceeding part of the window's first segment
            pushOut(curr.getStart());

            // We clear the monotonic edge
            Node<Double, R> newNode = makeMonotonic(t, curr.getValue());

            // We can add to the Window the pair (lastT, currV)
            w.addLast(newNode);
        }
    }

    private void pushOut(double currT) {
        if(!w.isEmpty() && currT - h.getEnd() > w.getFirst().getStart()) {
            Update<Double, R> oldFirst = pushUpdate();
            updates.add(oldFirst);
            w.addFirst(new Node<>(currT - h.getEnd(),
                                  oldFirst.getValue()));
        }
    }

    private Node<Double, R> makeMonotonic(Double lastT, R currV) {
        while(!w.isEmpty() && !op.apply(w.getLast().getValue(), currV)
                .equals(w.getLast().getValue()))
        {
            lastT = w.getLast().getStart();
            currV = op.apply(w.getLast().getValue(), currV);
            w.removeLast();
        }

        return new Node<>(lastT, currV);
    }

    private Update<Double, R> pushUpdate()
    {
        Node<Double, R> f = w.removeFirst();
        Double end = hEnd;
        if(!w.isEmpty())
            end = w.getFirst().getStart();

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
    }

}
