package eu.quanticol.moonlight.algorithms.online;

import eu.quanticol.moonlight.domain.AbstractInterval;
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
                                          AbstractInterval<Double> i,
                                          BinaryOperator<R> op)
    {
        List<Update<Double, R>> updates = new ArrayList<>();
        DiffIterator<SegmentInterface<Double, R>> itr = s.diffIterator();


        Double hStart = u.getStart() - i.getEnd();                              // Numeric OP
        Double hEnd = u.getEnd() - i.getStart();                                // Numeric OP

        Deque<SegmentInterface<Double, R>> window = new ArrayDeque<>();

        while(itr.hasNext()) {
            SegmentInterface<Double, R> curr = itr.next();
            SegmentInterface<Double, R> prev = tryPeekPrevious(itr, curr);

            if(curr.getStart() >= hEnd)        // We exited the update horizon,
                break;                         // nothing will change from here

            //1 - when the window gets full, push output
            if(curr.getStart() >= hStart)
                updates.add(genUpdate(curr.getStart(), hStart, hEnd,
                                      u.getValue(), itr));

            //2 - if current value is the relative max/min than previous
            if(op.apply(curr.getValue(), prev.getValue())
                 .equals(curr.getValue())
               && !curr.equals(prev))
               clearWindow(window, curr.getValue(), op);

            window.addLast(curr);

            // 3 - if the current item is at the ending edge of the window
            if(curr.getStart() == window.getFirst().getStart() + i.getEnd())    // Numeric OP
                window.removeFirst();
        }
        return updates;
    }

    private static <R extends Comparable<R>>
    Update<Double, R> genUpdate(Double start, Double hStart, Double hEnd,
                                R newValue,
                                DiffIterator<SegmentInterface<Double, R>> itr)
    {
        double currStart = Math.min(start, hStart);
        Double newStart = Math.max(0.0, currStart);
        Double currEnd = tryPeekNextStart(itr, Double.POSITIVE_INFINITY);
        Double newEnd = Math.min(currEnd, hEnd);

        return new Update<>(newStart, newEnd, newValue);
    }

    private static <R extends Comparable<R>>
    void clearWindow(Deque<SegmentInterface<Double, R>> window,
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

}
