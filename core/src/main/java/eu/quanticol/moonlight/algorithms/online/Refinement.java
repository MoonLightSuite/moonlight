package eu.quanticol.moonlight.algorithms.online;

import eu.quanticol.moonlight.signal.online.*;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.function.BiPredicate;

public class Refinement {
    private Refinement() {}     // hidden constructor

    public static <V> boolean refine(TimeChain<Double, V> s,
                                     Update<Double, V> u,
                                     BiPredicate<V, V> refinable)
    {
        if(u.getStart() > u.getEnd()) {
            throw new IllegalArgumentException("Invalid update time span");
        }
        DiffIterator<SegmentInterface<Double, V>> itr =
                s.diffIterator();
        SegmentInterface<Double, V> current = itr.next();

        V prevV = current.getValue();

        if(u.getStart().equals(u.getEnd()))
            return false;

        boolean done = false;

        while (itr.hasNext()) {
            if(doRefine(itr, current, u.getStart(), u.getEnd(), u.getValue(),
                        refinable, prevV))
            {
                done = true;
                break;
            }

            // Save the "next" as the new "current".
            prevV = current.getValue();
            current = itr.next();
        }

        if(!done) // To handle single-segment signals
            doRefine(itr, current, u.getStart(), u.getEnd(), u.getValue(),
                     refinable, prevV);

        return !s.hasChanged();
    }

    /**
     * Refinement logic
     *
     * @param itr segment iterator
     * @param curr current segment
     * @param from starting time of the update
     * @param to ending time of the update
     * @param vNew new value of the update
     * @return true when the update won't affect the signal anymore,
     *         false otherwise.
     */
    private static <V> boolean doRefine(
            DiffIterator<SegmentInterface<Double, V>> itr,
            SegmentInterface<Double, V> curr,
            double from, double to, V vNew, BiPredicate<V, V> refinable, V prevV)
    {
        double t = curr.getStart();
        double tNext = Double.POSITIVE_INFINITY;

        if(itr.hasNext())
            tNext = itr.peekNext().getStart();

        V v = curr.getValue();

        // Case 1 - `from` in (t, tNext):
        //          This means the update starts in the current segment
        if(t < from && tNext > from && refinable.test(v, vNew)) {
            add(itr, from, vNew);
//            if(!itr.hasNext() || (tNext > to && !v.equals(prevV)))
//                itr.add(new TimeSegment<>(to, v));
//            return false;
        }
        // Case 2 - from  == t:
        //          This means the current segment starts exactly at
        //          update time, therefore, its value must be updated
        if(t == from) {
            update(itr, t, v, vNew, refinable, prevV);
        }

        // Case 3 - t  in (from, to):
        //          This means the current segment starts within the update
        //          horizon and must therefore be updated
        if(t > from && t < to && refinable.test(v, vNew) && !v.equals(vNew)) {
            remove(itr);
        }

        // General Sub-case - to < tNext:
        //          This means the current segment contains the end of the
        //          area to update. Therefore, we must add a segment for the
        //          last part of the segment.
        //          From now on the signal will not change.
        if(to < tNext && t != to) {
            add(itr, to, v);
            return true;
        }

        // Case 4 - t  >= to:
        //          The current segment is beyond the update horizon,
        //          from now on, the signal will not change.
        return t >= to;
    }

    /**
     * Method for checking whether the provided interval refines the current
     * one, and to update it accordingly.
     * @param itr iterator of the signal segments
     * @param t current time instant
     * @param v current value
     * @param vNew new value from the update
     */
    private static <V>
    void update(DiffIterator<SegmentInterface<Double, V>> itr,
                double t, V v, V vNew, BiPredicate<V, V> refinable, V prevV)
    {
        //redundant updates can be ignored
        if(!v.equals(vNew)) {
            if(prevV.equals(vNew) && refinable.test(v, vNew)) {
                remove(itr);
            } else if (refinable.test(v, vNew)) {
                SegmentInterface<Double, V> s = new TimeSegment<>(t, vNew);
                itr.set(s);
            } else {
                throw new UnsupportedOperationException("Refining interval: " +
                        vNew + " is wider than " +
                        "the original:" + v);
            }

            if(itr.hasNext()) {
                if(itr.next().getValue().equals(vNew))
                    itr.remove();
                else
                    itr.previous();
            }
        }
    }

    /**
     * Removes the last object seen by the iterator.
     * Note that the iterator is one step ahead, so we have to bring it back
     * first.
     * @param itr iterator to update
     */
    private static <V>
    void remove(DiffIterator<SegmentInterface<Double, V>> itr)
    {
        itr.previous();
        itr.remove();
    }

    private static <V> void add(DiffIterator<SegmentInterface<Double, V>> itr,
                                Double start, V vNew)
    {
        if(!itr.peekPrevious().getValue().equals(vNew)) {
            itr.add(new TimeSegment<>(start, vNew));
            if(itr.hasNext() && itr.peekNext().getValue().equals(vNew)) {
                itr.next();
                remove(itr);
                itr.previous();
            }
        }
    }


    public static <T extends Comparable<T> & Serializable, V>
    TimeChain<T, V> select(TimeChain<T, V> segments, T from, T to)
    {
        int start = 0;
        int end = 1;

        DiffIterator<SegmentInterface<T, V>> itr = segments.diffIterator();

        do {
            SegmentInterface<T, V> current = itr.next();

            // We went too far, the last returned is the last one useful
            if(current.getStart().compareTo(to) > 0) {
                end = itr.previousIndex();
                break;
            }

            // So far no interesting segments, we move on
            if(current.getStart().compareTo(from) < 0)
                start = itr.previousIndex();

            // Last segment, this is necessarily the last interesting one.
            if(itr.tryPeekNext(current).equals(current))
                end = itr.previousIndex() + 1;

        } while(itr.hasNext());

        return segments.subChain(start, end, to);
    }
}
