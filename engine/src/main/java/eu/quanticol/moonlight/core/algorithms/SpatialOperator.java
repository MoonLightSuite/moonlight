package eu.quanticol.moonlight.core.algorithms;

import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;


public abstract class SpatialOperator
        <T extends Comparable<T> & Serializable, S, R>
{
    protected final LocationService<T, S> locSvc;
    protected final Function<SpatialModel<S>, DistanceStructure<S, ?>> dist;
    protected final BiFunction<IntFunction<R>,
            DistanceStructure<S, ?>,
            List<R>> op;

    protected Pair<T, SpatialModel<S>> currSpace;
    protected Pair<T, SpatialModel<S>> nextSpace;


    protected SpatialOperator(@NotNull LocationService<T, S> locationService,
                              Function<SpatialModel<S>,
                                      DistanceStructure<S, ?>> distance,
                              BiFunction<IntFunction<R>,
                                      DistanceStructure<S, ?>,
                                      List<R>> operator)
    {
        locSvc = locationService;
        dist = distance;
        op = operator;
    }

    protected void doCompute(T t, T tNext, List<R> value,
                           FiveParameterFunction<T, T, DistanceStructure<S, ?>,
                                   IntFunction<R>,
                                   Iterator<Pair<T, SpatialModel<S>>>> op)
    {
        Iterator<Pair<T, SpatialModel<S>>> spaceItr = locSvc.times();
        IntFunction<R> spatialSignal = value::get;
        tNext = seekSpace(t, tNext, spaceItr);
        SpatialModel<S> sm = currSpace.getSecond();
        DistanceStructure<S, ?> f = dist.apply(sm);

        op.accept(t, tNext, f, spatialSignal, spaceItr);
    }

    @FunctionalInterface
    public interface FiveParameterFunction<T, U, V, W, X> {
        void accept(T t, U u, V v, W w, X x);
    }

    protected T seekSpace(T start, T end,
                        Iterator<Pair<T, SpatialModel<S>>> spaceItr)
    {
        currSpace = spaceItr.next();
        getNext(spaceItr);
        while(isNextSpaceBeforeTime(start)) {
            currSpace = nextSpace;
            nextSpace = getNext(spaceItr);
        }
        return fromNextSpaceOrFallback(end);
    }

    protected abstract void addResult(T start, T end, List<R> value);

    private boolean isNextSpaceBeforeTime(T start) {
        return nextSpace != null && nextSpace.getFirst().compareTo(start) <= 0;
    }

    private T fromNextSpaceOrFallback(T fallback) {
        if(nextSpace != null)
            return nextSpace.getFirst();
        return fallback;
    }


    protected Iterator<Pair<T, SpatialModel<S>>> getSpaceIterator() {
        return locSvc.times();
    }



    /**
     * Returns the next element if there is one, otherwise null
     * @param itr Location Service Iterator
     * @param <S> Spatial Domain
     * @return Next element of the Location Service
     */
    protected static <T, S> Pair<T, SpatialModel<S>> getNext(
            Iterator<Pair<T, SpatialModel<S>>> itr)
    {
        return (itr.hasNext() ? itr.next() : null);
    }
}
