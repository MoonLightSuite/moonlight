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

    protected abstract void addResult(T start, T end, List<R> value);

    protected void moveAndCompute(T tNext,
                                  IntFunction<R> spatialSignal,
                                  Iterator<Pair<T, SpatialModel<S>>> spaceItr)
    {
        while (isNextSpaceModelWithinHorizon(tNext)) {
            shiftSpatialModel(spaceItr);
            T t = currSpace.getFirst();
            DistanceStructure<S, ?>  f = getDistanceStructure();

//            if(isNextSpaceModelMeaningful()) {
//                tNext = nextSpace.getFirst();
                addResult(t, tNext, op.apply(spatialSignal, f));
//            }
        }
    }

    protected void shiftSpatialModel(Iterator<Pair<T, SpatialModel<S>>> spaceItr)
    {
        currSpace = nextSpace;
        nextSpace = getNext(spaceItr);
    }

    protected void computeOp(T t, T tNext,
                             DistanceStructure<S, ?> f,
                             IntFunction<R> spatialSignal,
                             Iterator<Pair<T, SpatialModel<S>>> spaceItr)
    {
        addResult(t, tNext, op.apply(spatialSignal, f));
        moveAndCompute(tNext, spatialSignal, spaceItr);
    }

    protected DistanceStructure<S, ?>  getDistanceStructure() {
        SpatialModel<S> sm = currSpace.getSecond();
        return dist.apply(sm);
    }

    @FunctionalInterface
    public interface FiveParameterFunction<T, U, V, W, X> {
        void accept(T t, U u, V v, W w, X x);
    }

    protected void seekSpace(T t, Iterator<Pair<T, SpatialModel<S>>> spaceItr) {
        currSpace = spaceItr.next();
        nextSpace = getNext(spaceItr);
        while(isNextSpaceBeforeTime(t)) {
            shiftSpatialModel(spaceItr);
        }
    }

    protected Iterator<Pair<T, SpatialModel<S>>> toFirstSpatialModel(T t) {
        Iterator<Pair<T, SpatialModel<S>>> spaceItr = getSpaceIterator();
        seekSpace(t, spaceItr);
        return spaceItr;
    }

    protected boolean isNextSpaceModelWithinHorizon(T tNext) {
        return nextSpace != null && isBeforeTime(tNext);
    }

    protected boolean isNextSpaceModelMeaningful() {
        return nextSpace != null && !isModelAtSameTime(currSpace, getNextT());
    }

    private boolean isNextSpaceBeforeTime(T time) {
        return nextSpace != null &&
                (isBeforeTime(time) || !isModelAtSameTime(currSpace, time));
    }

    protected boolean isNextSpaceModelAtSameTime(T time) {
        return nextSpace != null && isModelAtSameTime(nextSpace, time);
    }

    protected boolean isBeforeTime(T time) {
        return getNextT().compareTo(time) < 0;
    }

    protected boolean isModelAtSameTime(Pair<T, SpatialModel<S>> model, T time)
    {
        return model.getFirst().equals(time);
    }

    protected T fromNextSpaceOrFallback(T fallback) {
        if(nextSpace != null)
            return getNextT();
        return fallback;
    }

    private T getNextT() {
        return nextSpace.getFirst();
    }

    protected Iterator<Pair<T, SpatialModel<S>>> getSpaceIterator() {
        return locSvc.times();
    }

    protected static <T, S> Pair<T, SpatialModel<S>> getNext(
            Iterator<Pair<T, SpatialModel<S>>> itr)
    {
        return (itr.hasNext() ? itr.next() : null);
    }
}
