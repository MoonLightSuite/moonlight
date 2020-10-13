package eu.quanticol.moonlight.algorithms;

import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.util.Pair;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Algorithm for Somewhere and Everywhere Computation
 */
public class WhereOperator {

    private WhereOperator() {} // Hidden constructor

    public static <S, R> SpatialTemporalSignal<R> computeDynamic(
            LocationService<S> l,
            Function<SpatialModel<S>, DistanceStructure<S, ?>> distance,
            BiFunction<Function<Integer, R>,
                       DistanceStructure<S, ?>,
                       List<R>> operator,
            SpatialTemporalSignal<R> s)
    {
        SpatialTemporalSignal<R> toReturn = new SpatialTemporalSignal<>(s.
                getNumberOfLocations());
        if (l.isEmpty()) {
            return toReturn;
        }

        ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
        Iterator<Pair<Double, SpatialModel<S>>> locSvcIterator = l.times();
        Pair<Double, SpatialModel<S>> current = locSvcIterator.next();
        Pair<Double, SpatialModel<S>> next = getNext(locSvcIterator);

        double time = cursor.getTime();

        while ((next != null) && (next.getFirst() <= time)) {
            current = next;
            next = getNext(locSvcIterator);
        }

        //Loop invariant: (current.getFirst() <= time) &&
        //                ((next == null) || (time < next.getFirst()))
        while (!cursor.completed() && !Double.isNaN(time)) {
            Function<Integer, R> spatialSignal = cursor.getValue();
            SpatialModel<S> sm = current.getSecond();
            DistanceStructure<S, ?> f = distance.apply(sm);
            toReturn.add(time, operator.apply(spatialSignal, f));
            double nextTime = cursor.forward();
            while ((next != null) && (next.getFirst() < nextTime)) {
                current = next;
                time = current.getFirst();
                next = getNext(locSvcIterator);
                f = distance.apply(current.getSecond());
                toReturn.add(time, operator.apply(spatialSignal, f));
            }
            time = nextTime;
            current = (next != null ? next : current);
            next = getNext(locSvcIterator);
        }
        //TODO: Manage end of signal!
        return toReturn;
    }

    /**
     * Returns the next element if there is one, otherwise null
     * @param iter Location Service Iterator
     * @param <S> Spatial Domain
     * @return Next element of the Location Service
     */
    private static <S> Pair<Double, SpatialModel<S>> getNext(
            Iterator<Pair<Double, SpatialModel<S>>> iter)
    {
        return (iter.hasNext() ? iter.next() : null);
    }
}
