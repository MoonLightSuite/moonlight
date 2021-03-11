package eu.quanticol.moonlight.algorithms.online;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.signal.online.DiffIterator;
import eu.quanticol.moonlight.signal.online.SegmentChain;
import eu.quanticol.moonlight.signal.online.SegmentInterface;
import eu.quanticol.moonlight.signal.online.Update;

import java.io.Serializable;
import java.util.*;
import java.util.function.BinaryOperator;

import static eu.quanticol.moonlight.algorithms.online.BooleanComputation.tryPeekNextStart;

/**
 * Note that the methods in this class require explicit time declaration to deal
 * with numeric operations which cannot be defined on Generic types, not even
 * on the <code>Number</code> class.
 *
 * That's because numeric operations (such as +, -, *, /, %) are defined on
 * primitive types, and Generic types do not support instantiations based on
 * primitive types.
 *
 * @see <a href="https://docs.oracle.com/javase/tutorial/java/generics/restrictions.html">Java Generics Restrictions</a>
 */
public class TemporalComputation {

    private TemporalComputation() {}    // hidden constructor

    public static <R extends Comparable<R>>
    List<Update<Double, R>> slidingWindow(SegmentChain<Double, R> s,
                                          Update<Double, R> u,
                                          Interval opHor,
                                          BinaryOperator<R> op)
    {
        List<Update<Double, R>> updates = new ArrayList<>();
        DiffIterator<SegmentInterface<Double, R>> itr = s.diffIterator();

        //Double hStart = u.getStart() - opHorizon.getEnd();
        double hEnd = u.getEnd() - opHor.getStart();               // Numeric OP
        double wSize = opHor.getEnd() - opHor.getStart();          // Numeric OP

        Deque<Update<Double, R>> w = new ArrayDeque<>();

        double t = 0.0;

        // While there are segments and
        // the update horizon ends after the current time-point
        while(itr.hasNext() && t < hEnd) {
            SegmentInterface<Double, R> curr = itr.next();
            SegmentInterface<Double, R> next = tryPeekNext(itr, curr);
            R currV = curr.getValue();

            // t >= next.start - op.horizon.start:
            //   We can skip this segment safely
            if(next != curr && t >= next.getStart() - opHor.getStart())
                continue;

            t = Math.max(t, next.getStart() - opHor.getEnd());
            if(!w.isEmpty())
                t = Math.min(t, Math.abs(curr.getStart() - opHor.getStart()));

            // We are exceeding the window size, we must pop left sides
            // of the window and we can propagate the related updates
            while(!w.isEmpty() && t - w.getFirst().getStart() > wSize) {
                updates.add(pushUp(w, hEnd));
                /*if(w.getFirst().getStart() < wSize){
                    Update<Double, R> oldFirst = w.removeFirst();
                    w.addFirst(new Update<>(oldFirst.getStart() - wSize, null, oldFirst.getValue()));
                }*/
            }

            double lastT = t;
            while(!w.isEmpty() && !op.apply(w.getLast().getValue(), currV)
                     .equals(w.getLast().getValue()))
            {
                lastT = w.getLast().getStart();
                currV = op.apply(w.getLast().getValue(), currV);
                w.removeLast();
            }

            // We can add to the Window the pair (lastT, currV)
            w.addLast(new Update<>(lastT, null, currV));
        }

        while(!w.isEmpty()) {
            updates.add(pushUp(w, hEnd));
        }

        return updates;
    }

    private static <R extends Comparable<R>>
    Update<Double, R> pushUp(Deque<Update<Double, R>> w, double e) {
        Update<Double, R> f = w.removeFirst();
        Double end = e;
        if(!w.isEmpty())
            end = w.getFirst().getStart();
        return new Update<>(f.getStart(), end, f.getValue());
    }

    /*public static <R extends Comparable<R>>
    List<Update<Double, R>> slidingWindowOld(SegmentChain<Double, R> s,
                                          Update<Double, R> u,
                                          Interval opHorizon,
                                          BinaryOperator<R> op)
    {
        List<Update<Double, R>> updates = new ArrayList<>();
        DiffIterator<SegmentInterface<Double, R>> itr = s.diffIterator();


        Double hStart = u.getStart() - opHorizon.getEnd();         // Numeric OP
        hStart = Math.max(0.0, hStart);
        Double hEnd = u.getEnd() - opHorizon.getStart();           // Numeric OP

        Deque<Update<Double, R>> window = new ArrayDeque<>();

        while(itr.hasNext()) {
            SegmentInterface<Double, R> curr = itr.next();
            SegmentInterface<Double, R> prev = tryPeekPrevious(itr, curr);

            if(curr.getStart() >= hEnd)        // We exited the update horizon,
                break;                         // nothing will change from here

            //1 - when the window gets full, push output
            if(curr.getStart() > hStart) {
                R newVal = //op.apply(curr.getValue(), u.getValue());
                            window.getFirst().getValue();
                if(!newVal.equals(curr.getValue()))
                    updates.add(genUpdate(curr.getStart(), hStart, hEnd,
                            op.apply(curr.getValue(), u.getValue()), itr));
            }

            //2 - if current value is the relative max/min than previous
            if(op.apply(curr.getValue(), prev.getValue())
                 .equals(curr.getValue())
               && !curr.equals(prev))
               clearWindow(window, curr.getValue(), op);

            updateWindow(window, curr, u, op);

            // 3 - if the current item is at the ending edge of the window
            if(curr.getStart() == window.getFirst().getStart()
                                         + opHorizon.getEnd())     // Numeric OP
                window.removeFirst();
        }

        if(updates.isEmpty() && !window.isEmpty()) {
            Update<Double, R> up = window.getFirst();
            updates.add(genUpdate(up.getStart(), hStart, hEnd, up.getValue(), itr));
        }
        return updates;
    }*/

    private static <R extends Comparable<R>>
    void updateWindow(Deque<Update<Double, R>> w,
                      SegmentInterface<Double, R> curr, Double t)
    {
        //TODO: end is not needed
        w.addLast(new Update<>(t, null, curr.getValue()));
    }

    private static <R extends Comparable<R>>
    Update<Double, R> genUpdate(Double start, Double hStart, Double hEnd,
                                R newValue,
                                DiffIterator<SegmentInterface<Double, R>> itr)
    {
        Double newStart = Math.max(start, hStart);
        Double currEnd = tryPeekNextStart(itr, Double.POSITIVE_INFINITY);
        Double newEnd = Math.min(currEnd, hEnd);

        return new Update<>(newStart, newEnd, newValue);
    }

    private static <R extends Comparable<R>>
    void clearWindow(Deque<Update<Double, R>> window,
                     R value, BinaryOperator<R> op)
    {
        window.removeLast();
        while(!window.isEmpty()) {
            // if the current element is not the max/min anymore, we can return
            if(!op.apply(value, window.getLast().getValue()).equals(value))
                break;
            window.removeLast();
        }
    }


    /**
     * Fail-safe method for fetching data from previous element (if exists).
     *
     * @param itr iterator to use for looking back
     * @param defaultValue value to return in case of failure
     * @param <T> time domain of interest, usually a <code>Number</code>
     * @param <V> domain of the returned value
     * @return the previous value if present, otherwise the default one.
     */
    static <T extends Comparable<T> & Serializable, V extends Comparable<V>>
    SegmentInterface<T, V> tryPeekPrevious(
            DiffIterator<SegmentInterface<T, V>> itr,
            SegmentInterface<T, V> defaultValue)
    {
        try {
            return itr.peekPrevious();
        } catch (NoSuchElementException e) {
            return defaultValue;
        }
    }

    /**
     * Fail-safe method for fetching data from previous element (if exists).
     *
     * @param itr iterator to use for looking back
     * @param defaultValue value to return in case of failure
     * @param <T> time domain of interest, usually a <code>Number</code>
     * @param <V> domain of the returned value
     * @return the previous value if present, otherwise the default one.
     */
    static <T extends Comparable<T> & Serializable, V extends Comparable<V>>
    SegmentInterface<T, V> tryPeekNext(
            DiffIterator<SegmentInterface<T, V>> itr,
            SegmentInterface<T, V> defaultValue)
    {
        try {
            return itr.peekNext();
        } catch (NoSuchElementException e) {
            return defaultValue;
        }
    }

}
