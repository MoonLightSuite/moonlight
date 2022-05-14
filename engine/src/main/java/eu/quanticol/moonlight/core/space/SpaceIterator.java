package eu.quanticol.moonlight.core.space;

import eu.quanticol.moonlight.core.base.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Function;


public class SpaceIterator<T extends Comparable<T>, S> {
    private final LocationService<T, S> locSvc;
    private final Function<SpatialModel<S>, DistanceStructure<S, ?>> dist;
    private Pair<T, SpatialModel<S>> currSpace;
    private Pair<T, SpatialModel<S>> nextSpace;
    private Iterator<Pair<T, SpatialModel<S>>> spaceItr;


    public SpaceIterator(@NotNull LocationService<T, S> locationService,
                         Function<SpatialModel<S>,
                                 DistanceStructure<S, ?>> distance) {
        locSvc = locationService;
        dist = distance;
    }

    public void init(T startingTime) {
        spaceItr = getSpaceIterator();
        seekSpace(startingTime);
    }

    private void seekSpace(T t) {
        currSpace = spaceItr.next();
        nextSpace = moveNext();
        while (isNextSpaceBeforeTime(t)) {
            shiftSpatialModel();
        }
    }

    public void shiftSpatialModel() {
        currSpace = nextSpace;
        nextSpace = moveNext();
    }

    public boolean isLocationServiceEmpty() {
        return locSvc.isEmpty();
    }

    public void forEach(T tNext,
                        BiConsumer<T, DistanceStructure<S, ?>> procedure) {
        while (isNextSpaceModelWithinHorizon(tNext)) {
            shiftSpatialModel();
            T t = getCurrentT();
            DistanceStructure<S, ?> f = generateDistanceStructure();
            procedure.accept(t, f);

//            if(isNextSpaceModelMeaningful()) {
//                tNext = nextSpace.getFirst();
//            }
        }
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
        if (nextSpace != null)
            return getNextT();
        return fallback;
    }

    private Iterator<Pair<T, SpatialModel<S>>> getSpaceIterator() {
        return locSvc.times();
    }

    private Pair<T, SpatialModel<S>> moveNext() {
        return spaceItr.hasNext() ? spaceItr.next() : null;
    }
}
