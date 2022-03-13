package eu.quanticol.moonlight.core.algorithms;

import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.core.base.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;


public abstract class SpaceIterator <T extends Comparable<T> & Serializable, S, R> {
    private final LocationService<T, S> locSvc;
    private final Function<SpatialModel<S>, DistanceStructure<S, ?>> dist;
    private final BiFunction<IntFunction<R>, DistanceStructure<S, ?>, List<R>> op;

    private Pair<T, SpatialModel<S>> currSpace;
    private Pair<T, SpatialModel<S>> nextSpace;
    private Iterator<Pair<T, SpatialModel<S>>> spaceItr;
    private BiFunction<T, T, R> resultAction;


    public SpaceIterator(@NotNull LocationService<T, S> locationService,
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

    public void init(T startingTime, BiFunction<T, T, R> resultStoringAction) {
        spaceItr = toFirstSpatialModel(startingTime);
        resultAction = resultStoringAction;
    }

    public boolean isLocationServiceEmpty() {
        return locSvc.isEmpty();
    }

    protected abstract void addResult(T start, T end, List<R> value);

    private void moveAndCompute(T tNext,
                                  IntFunction<R> spatialSignal,
                                  Iterator<Pair<T, SpatialModel<S>>> spaceItr)
    {
        while (isNextSpaceModelWithinHorizon(tNext)) {
            shiftSpatialModel(spaceItr);
            T t = currSpace.getFirst();
            DistanceStructure<S, ?>  f = generateDistanceStructure();

//            if(isNextSpaceModelMeaningful()) {
//                tNext = nextSpace.getFirst();
                addResult(t, tNext, op.apply(spatialSignal, f));
//            }
        }
    }

    public void shiftSpatialModel(Iterator<Pair<T, SpatialModel<S>>> spaceItr)
    {
        currSpace = nextSpace;
        nextSpace = moveNext(spaceItr);
    }

    public void computeOp(T t, T tNext,
                             DistanceStructure<S, ?> f,
                             IntFunction<R> spatialSignal,
                             Iterator<Pair<T, SpatialModel<S>>> spaceItr)
    {
        addResult(t, tNext, op.apply(spatialSignal, f));
        moveAndCompute(tNext, spatialSignal, spaceItr);
    }

    public T getCurrentT() {
        return currSpace.getFirst();
    }

    public T getNextT() {
        return nextSpace.getFirst();
    }

    public DistanceStructure<S, ?> generateDistanceStructure() {
        SpatialModel<S> sm = currSpace.getSecond();
        return dist.apply(sm);
    }

    private void seekSpace(T t, Iterator<Pair<T, SpatialModel<S>>> spaceItr) {
        currSpace = spaceItr.next();
        nextSpace = moveNext(spaceItr);
        while(isNextSpaceBeforeTime(t)) {
            shiftSpatialModel(spaceItr);
        }
    }

    protected Iterator<Pair<T, SpatialModel<S>>> toFirstSpatialModel(T t) {
        Iterator<Pair<T, SpatialModel<S>>> spaceItr = getSpaceIterator();
        seekSpace(t, spaceItr);
        return spaceItr;
    }

    public boolean isNextSpaceModelWithinHorizon(T tNext) {
        return nextSpace != null && isBeforeTime(tNext);
    }

    public boolean isNextSpaceModelMeaningful() {
        return nextSpace != null &&
                !isModelAtSameTime(currSpace, getNextT());
    }

    private boolean isNextSpaceBeforeTime(T time) {
        return nextSpace != null &&
                (isBeforeTime(time) || !isModelAtSameTime(currSpace, time));
    }

    public boolean isNextSpaceModelAtSameTime(T time) {
        return nextSpace != null && isModelAtSameTime(nextSpace, time);
    }

    private boolean isBeforeTime(T time) {
        return getNextT().compareTo(time) < 0;
    }

    private boolean isModelAtSameTime(Pair<T, SpatialModel<S>> model, T time)
    {
        return model.getFirst().equals(time);
    }

    public T fromNextSpaceOrFallback(T fallback) {
        if(nextSpace != null)
            return getNextT();
        return fallback;
    }

    protected Iterator<Pair<T, SpatialModel<S>>> getSpaceIterator() {
        return locSvc.times();
    }

    private static <T, S> Pair<T, SpatialModel<S>> moveNext(
            Iterator<Pair<T, SpatialModel<S>>> itr)
    {
        return (itr.hasNext() ? itr.next() : null);
    }
}
