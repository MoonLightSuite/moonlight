package eu.quanticol.moonlight.signal;

import java.util.LinkedList;
import java.util.ListIterator;

import eu.quanticol.moonlight.domain.AbstractInterval;

public class OnlineSignal<T extends Comparable<T>> {

    // An invariant of the segments list is the fact that all segments
    // start at strictly monotonic increasing times
    // TODO: It should be enforced by mutators and it is trivially
    //  satisfied at the beginning, i.e. with one segment
    private final LinkedList<ImmutableSegment<AbstractInterval<T>>> segments;


    /*public OnlineSignal(double start, double end,
                        T minValue, T maxValue)
    {
        this.segments = new LinkedList<>();
        AbstractInterval<T> value = new AbstractInterval<>(minValue, maxValue);
        this.segments.add(new ImmutableSegment<>(start, value));
    }*/

    public OnlineSignal(T minValue, T maxValue) {
        this.segments = new LinkedList<>();
        AbstractInterval<T> value = new AbstractInterval<>(minValue, maxValue);
        this.segments.add(new ImmutableSegment<>(0, value));
    }


    /**
     * Returns the time point where the signal starts.
     *
     * @return the time point where the signal starts.
     */
    public double getStart() {
        assert segments.peekFirst() != null;
        return segments.peekFirst().getStart();
    }

    /**
     * Returns the time point where the signal ends.
     *
     * @return the time point where the signal ends.
     */
    /*public double end() {
        return segments.peekLast().end();
    }*/

    /**
     * Updates the content of the signal in the time interval <code>[from,to)</code> with new interval value
     * <code>i</code>. An {@link IllegalArgumentException} is thrown whenever the new interval is not a subset of
     * the currents ones in the time interval  <code>[from,to)</code>.
     *
     * @param from the initial time of the update.
     * @param to the ending time of the update.
     * @param i the new value interval.
     * @return the list of updated segments.
     */
   /* public List<ImmutableSegment<AbstractInterval<T>>> refine(double from, double to, AbstractInterval<T> i) {
        if(from > to) {
            throw new UnsupportedOperationException("Invalid update time span");
        }

        ImmutableSegment<AbstractInterval<T>> next;
        ImmutableSegment<AbstractInterval<T>> curr = null;
        ListIterator<ImmutableSegment<AbstractInterval<T>>> itr =
                segments.listIterator();

        while (itr.hasNext()) {
            next = itr.next();
            if (curr != null) { // Passed the first iteration
               if(doRefine(curr.getStart(), next.getStart(), curr.getValue(),
                           from, to, i, itr))
                   break;
            }
            curr = next; // Save what was the "next" as the next "current".
        }

        if(curr == null)
            throw new UnsupportedOperationException("Empty signal provided");
        else
            doRefine(curr.getStart(), Double.POSITIVE_INFINITY, curr.getValue(),
                    from, to, i, itr);

        return null;
    }*/

    /**
     * Requires: !segments.isEmpty()
     * @param from
     * @param to
     * @param i
     */
    public void refine(double from, double to, AbstractInterval<T> i) {
        if(from > to) {
            throw new UnsupportedOperationException("Invalid update time span");
        }
        ListIterator<ImmutableSegment<AbstractInterval<T>>> itr =
                segments.listIterator();
        ImmutableSegment<AbstractInterval<T>> current = itr.next();

        while (itr.hasNext()) {
            // We peek the next starting time
            double tNext = itr.next().getStart();
            // But we bring back the iterator to the current segment
            itr.previous();

            if(doRefine(itr, current, tNext, from, to, i))
                break;

            // Save the "next" as the next "current".
            current = itr.next();
        }

        doRefine(itr, current, Double.POSITIVE_INFINITY, from, to, i);
    }

    /**
     * Refinement logic
     *
     * @param itr segment iterator
     * @param curr current segment
     * @param tNext starting time of next segment
     * @param from starting time of the update
     * @param to ending time of the update
     * @param vNew new value of the update
     * @return true when the update won't affect the signal anymore,
     *         false otherwise.
     */
    private boolean doRefine(
            ListIterator<ImmutableSegment< AbstractInterval<T>>> itr,
            ImmutableSegment<AbstractInterval<T>> curr,
            double tNext, double from, double to, AbstractInterval<T> vNew)
    {
        double t = curr.getStart();
        AbstractInterval<T> v = curr.getValue();

        // Case 1 - from in (t, tNext):
        //          This means the update starts in the current segment
        if(t < from && tNext > from) {
            itr.add(new ImmutableSegment<>(from, vNew));
        }
        // Case 2 - from  == t:
        //          This means the current segment starts exactly at
        //          update time, therefore, its value must be updated
        if(t == from) {
            update(itr, t, v, vNew);
        }

        // Case 3 - t  in (from, to):
        //          This means the current segment starts within the update
        //          horizon and must therefore be updated
        if(t > from && t < to) {
            remove(itr);
        }

        // General Subcase - to < tNext:
        //          This means the current segment contains the end of the
        //          area to update. Therefore, we must add a segment for the
        //          last part of the segment.
        //          From now on the signal will not change.
        if(to < tNext && t != to) {
            itr.add(new ImmutableSegment<>(to, v));
            return true;
        }

        // Case 4 - t  > to:
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
    private void update(
            ListIterator<ImmutableSegment< AbstractInterval<T>>> itr,
            double t, AbstractInterval<T> v,
            AbstractInterval<T> vNew)
    {
        if(v.contains(vNew)) {
            itr.set(new ImmutableSegment<>(t, vNew));
        } else {
            throw new UnsupportedOperationException(
                    "Refining interval is wider than the original");
        }
    }

    /**
     * Removes the last object seen by the iterator.
     * Note that the iterator is one step ahead, so we have to bring it back
     * first.
     * @param itr iterator to update
     */
    private void remove(ListIterator<ImmutableSegment<AbstractInterval<T>>> itr)
    {
        itr.previous();
        itr.remove();
    }

    /**
     * Set the value <code>value</code> in the time interval <code>[from,to)</code>. An {@link IllegalArgumentException}
     * is thrown whenever the value <code>i</code> is not in the current intervals in the time interval
     * <code>[from,to)</code>.
     *
     * @param from the initial time of the update.
     * @param to the ending time of the update.
     * @param value new value
     * @return the list of updated segments.
     */
    /*public List<ImmutableSegment<AbstractInterval<T>>> refine(double from, double to, T value) {
        return refine(from, to, new AbstractInterval<>(value,value));
    }*/

    /**
     * Returns the interval of valid signal values at time <code>t</code>. An {@link IllegalArgumentException}
     * is thrown whenever the value <code>t</code> is outside the signal time boundaries.
     *
     * @param t time instant.
     * @return the interval of valid signal values at time <code>t</code>.
     */
    public AbstractInterval<T> getValueAt(double t) {
        ListIterator<ImmutableSegment<AbstractInterval<T>>> itr =
                segments.listIterator();
        ImmutableSegment<AbstractInterval<T>> current = null;

        while (itr.hasNext()) {
            current = itr.next();
            if (current.getStart() > t) {
                // We went too far, we have to look at the previous element
                // So we have to move the iterator twice back
                // (as we are now looking backwards)
                itr.previous();
                return itr.previous().getValue();
            }
        }

        if(current != null)
            return current.getValue();
        else
            throw new UnsupportedOperationException("Empty signal provided");
    }

    @Override
    public String toString() {
        return "OnlineSignal{" + "segments=" + segments + "}";
    }
}
