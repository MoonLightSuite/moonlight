package eu.quanticol.moonlight.signal;

import java.util.LinkedList;
import java.util.List;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.Interval;

public class OnlineSignal<T extends Comparable<T>> {

    private LinkedList<ImmutableSegment<AbstractInterval<T>>> segments;


    public OnlineSignal(double start, double end, T minValue, T maxValue) {
        this.segments = new LinkedList<>();
        this.segments.add(new ImmutableSegment<>(start, end, new AbstractInterval<T>(minValue, maxValue)));
    }


    /**
     * Returns the time point where the signal starts.
     *
     * @return the time point where the signal starts.
     */
    public double start() {
        return segments.peekFirst().start();
    }

    /**
     * Returns the time point where the signal ends.
     *
     * @return the time point where the signal ends.
     */
    public double end() {
        return segments.peekLast().end();
    }

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
    public List<ImmutableSegment<AbstractInterval<T>>> update(double from, double to, AbstractInterval<T> i) {
        //Trovare partendo dal fondo il segmento s2 che include to
        //Trovare partendo da s2 il segmento s1 che include from
        //Spezzare s1 in s1' e s1'' dove s1'.end = from e s1''.start = from
        //Spezzare s2 in s2' e s2'' dove s2'.end = to e s2''.start = tro
        //Aggiornare tutti i segmenti tra s1'' e s2'
        return null;
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
    public List<ImmutableSegment<AbstractInterval<T>>> update(double from, double to, T value) {
        return update(from,to,new AbstractInterval<>(value,value));
    }

    /**
     * Returns the interval of valid signal values at time <code>t</code>. An {@link IllegalArgumentException}
     * is thrown whenever the value <code>t</code> is outside the signal time boundaries.
     *
     * @param t signal time.
     * @return the interval of valid signal values at time <code>t</code>.
     */
    public Interval getValueAt(double t) {
        return null;
    }

}
