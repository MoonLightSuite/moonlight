package eu.quanticol.moonlight.algorithms.online;

import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.signal.online.DiffIterator;
import eu.quanticol.moonlight.signal.online.SegmentChain;
import eu.quanticol.moonlight.signal.online.SegmentInterface;
import eu.quanticol.moonlight.signal.online.Update;
import eu.quanticol.moonlight.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SpatialComputation {

    private SpatialComputation() {}    // hidden constructor

    public static <S, R>
    List<Update<Double, R>> computeDynamic(
        LocationService<S> l,
        Function<SpatialModel<S>, DistanceStructure<S, ?>> distance,
        BiFunction<Function<Integer, R>, DistanceStructure<S, ?>, List<R>> op,
        Update<Double, R> u,
        SegmentChain<Double, R> s)
    {
        List<Update<Double, R>> results = new ArrayList<>();

        if (l.isEmpty())
            return results;

        //TODO: only Double dependence
        Iterator<Pair<Double, SpatialModel<S>>> spaceItr = l.times();

        Pair<Double, SpatialModel<S>> currSpace = spaceItr.next();
        Pair<Double, SpatialModel<S>> nextSpace = getNext(spaceItr);



        DiffIterator<SegmentInterface<Double, R>> itr = s.diffIterator();
        SegmentInterface<Double, R> curr = itr.next();
        Double t = curr.getStart();

        while ((nextSpace != null) &&
               (nextSpace.getFirst().compareTo(t) <= 0)) {
            currSpace = nextSpace;
            nextSpace = getNext(spaceItr);
        }

        /*while(itr.hasNext() && t < u.getEnd()) {
            SegmentInterface<Double, R> next = itr.tryPeekNext(curr);
            Double nextTime = next.getStart();
            Function<Integer, R> spatialSignal = i -> curr.getValue().get(i);

            SpatialModel<S> sm = currSpace.getSecond();
            DistanceStructure<S, ?> f = distance.apply(sm);

            results.add(
                    new Update<>(t, nextTime,
                            op.apply(spatialSignal, f)));
            //TODO: toReturn.add(start, op.apply(spatialSignal, f));

            while ((nextSpace != null) && (nextSpace.getFirst() < nextTime)) {
                currSpace = nextSpace;
                t = currSpace.getFirst();
                nextSpace = getNext(spaceItr);
                f = distance.apply(currSpace.getSecond());
                //TODO: toReturn.add(t, op.apply(spatialSignal, f));
            }
            currSpace = (nextSpace != null ? nextSpace : currSpace);
            nextSpace = getNext(spaceItr);

        }*/

        return results;
    }

    /**
     * Returns the next element if there is one, otherwise null
     * @param itr Location Service Iterator
     * @param <S> Spatial Domain
     * @return Next element of the Location Service
     */
    private static <T, S> Pair<T, SpatialModel<S>> getNext(
            Iterator<Pair<T, SpatialModel<S>>> itr)
    {
        return (itr.hasNext() ? itr.next() : null);
    }
}
