package eu.quanticol.moonlight.core.algorithms;

import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.core.base.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;


public class SpaceIterator<T extends Comparable<T>, S, R> {
    private final LocationService<T, S> locSvc;
    private final Function<SpatialModel<S>, DistanceStructure<S, ?>> dist;
    private final BiFunction<List<R>, DistanceStructure<S, ?>, List<R>> op;

    private Pair<T, SpatialModel<S>> currSpace;
    private Pair<T, SpatialModel<S>> nextSpace;
    private Iterator<Pair<T, SpatialModel<S>>> spaceItr;
    private TriConsumer<T, T, List<R>> resultAction;


    public SpaceIterator(@NotNull LocationService<T, S> locationService,
                            Function<SpatialModel<S>,
                                      DistanceStructure<S, ?>> distance,
                            BiFunction<List<R>,
                                      DistanceStructure<S, ?>,
                                      List<R>> operator)
    {
        locSvc = locationService;
        dist = distance;
        op = operator;
    }

    public void init(T startingTime,
                     TriConsumer<T, T, List<R>> resultStoringAction)
    {
        toFirstSpatialModel(startingTime);
        resultAction = resultStoringAction;
    }

    public boolean isLocationServiceEmpty() {
        return locSvc.isEmpty();
    }

    private void moveAndCompute(T tNext, List<R> spatialSignal) {
        while (isNextSpaceModelWithinHorizon(tNext)) {
            shiftSpatialModel();
            T t = currSpace.getFirst();
            DistanceStructure<S, ?>  f = generateDistanceStructure();

//            if(isNextSpaceModelMeaningful()) {
//                tNext = nextSpace.getFirst();
            resultAction.accept(t, tNext, op.apply(spatialSignal, f));
//            }
        }
    }

    public void shiftSpatialModel() {
        currSpace = nextSpace;
        nextSpace = moveNext();
    }

    public void computeOp(T t, T tNext,
                          DistanceStructure<S, ?> f,
                          List<R> spatialSignal)
    {
        resultAction.accept(t, tNext, op.apply(spatialSignal, f));
        moveAndCompute(tNext, spatialSignal);
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

    private void seekSpace(T t) {
        currSpace = spaceItr.next();
        nextSpace = moveNext();
        while(isNextSpaceBeforeTime(t)) {
            shiftSpatialModel();
        }
    }

    private void toFirstSpatialModel(T t) {
        spaceItr = getSpaceIterator();
        seekSpace(t);
    }

    public boolean isNextSpaceModelWithinHorizon(T tNext) {
        return nextSpace != null && isBeforeTime(tNext);
    }

    public boolean isNextSpaceModelMeaningful() {
        return nextSpace != null && !isModelAtSameTime(currSpace, getNextT());
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

    private boolean isModelAtSameTime(Pair<T, SpatialModel<S>> model, T time) {
        return model.getFirst().equals(time);
    }

    public T fromNextSpaceOrFallback(T fallback) {
        if(nextSpace != null)
            return getNextT();
        return fallback;
    }

    private Iterator<Pair<T, SpatialModel<S>>> getSpaceIterator() {
        return locSvc.times();
    }

    private Pair<T, SpatialModel<S>> moveNext()
    {
        return (spaceItr.hasNext() ? spaceItr.next() : null);
    }

    @FunctionalInterface
    public interface TriConsumer<T, U, V> {
        /**
         * Performs this operation on the given arguments.
         *
         * @param t the first input argument
         * @param u the second input argument
         */
        void accept(T t, U u, V v);
    }
}
