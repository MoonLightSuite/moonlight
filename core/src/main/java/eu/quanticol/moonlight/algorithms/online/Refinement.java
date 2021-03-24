package eu.quanticol.moonlight.algorithms.online;

import eu.quanticol.moonlight.signal.online.*;

import java.util.NoSuchElementException;
import java.util.function.BiPredicate;

public class Refinement {
    private Refinement() {}     // hidden constructor

    public static <V> boolean refine(SegmentChain<Double, V> s,
                                     Update<Double, V> u,
                                     BiPredicate<V, V> refinable)
    {
        if(u.getStart() > u.getEnd()) {
            throw new IllegalArgumentException("Invalid update time span");
        }
        DiffIterator<SegmentInterface<Double, V>> itr =
                s.diffIterator();
        SegmentInterface<Double, V> current = itr.next();

        boolean done = false;

        while (itr.hasNext()) {
            if(doRefine(itr, current, u.getStart(), u.getEnd(), u.getValue(),
                        refinable))
            {
                done = true;
                break;
            }

            // Save the "next" as the next "current".
            current = itr.next();
        }

        if(!done) // To handle single-segment signals
            doRefine(itr, current, u.getStart(), u.getEnd(), u.getValue(),
                     refinable);

        return !itr.getChanges().isEmpty();
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
            double from, double to, V vNew, BiPredicate<V, V> refinable)
    {
        double t = curr.getStart();
        double tNext = Double.POSITIVE_INFINITY;
        try {
            tNext = itr.peekNext().getStart();
        } catch(NoSuchElementException ignored) {
            // Exception handled by default value of tNext
        }

        V v = curr.getValue();

        // Case 1 - `from` in (t, tNext):
        //          This means the update starts in the current segment
        if(t < from && tNext > from) {
            add(itr, from, vNew);
            return false;
        }
        // Case 2 - from  == t:
        //          This means the current segment starts exactly at
        //          update time, therefore, its value must be updated
        if(t == from) {
            update(itr, t, v, vNew, refinable);
        }

        // Case 3 - t  in (from, to):
        //          This means the current segment starts within the update
        //          horizon and must therefore be updated
        if(t > from && t < to) {
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
                double t, V v, V vNew, BiPredicate<V, V> refinable)
    {
        if(refinable.test(v, vNew)) {
            ImmutableSegment<V> s = new ImmutableSegment<>(t, vNew);

            SegmentInterface<Double, V> p = itr.peekPrevious();

            if(!s.equals(p))
                itr.set(s);
        } else {
            throw new UnsupportedOperationException("Refining interval: " +
                    vNew + " is wider than " +
                    "the original:" + v);
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
                                Double start, V value)
    {
        if(!itr.peekPrevious().getValue().equals(value))
            itr.add(new ImmutableSegment<>(start, value));
    }
}
