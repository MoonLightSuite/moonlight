package eu.quanticol.moonlight.core.space;

import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.Triple;

import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/**
 * Algorithmic class for computing the reach operator
 * @param <E> weight type of the edges of the spatial model
 * @param <M> metric type considered by the distance structure
 * @param <R> signal interpretation type
 */
public class ReachAlgorithm<E, M, R> {
    private final SpatialModel<E> model;
    private final SignalDomain<R> signalDomain;
    private final IntFunction<R> leftSpatialSignal;
    private final IntFunction<R> rightSpatialSignal;
    private final DistanceStructure<E, M> distStr;
    private final DistanceDomain<M> distanceDomain;
    private final List<Triple<Integer, M, R>> reachabilityQueue;

    /**
     * This list is quite special: the i-th element of the list denotes
     * the set of optimal values for each distance from location i.
     */
    private final List<Map<M, R>> reachabilityFunction;

    public ReachAlgorithm(DistanceStructure<E, M> distStr,
                          SignalDomain<R> signalDomain,
                          IntFunction<R> leftSpatialSignal,
                          IntFunction<R> rightSpatialSignal) {
        this.leftSpatialSignal = leftSpatialSignal;
        this.rightSpatialSignal = rightSpatialSignal;
        this.distStr = distStr;
        this.model = distStr.getModel();
        this.distanceDomain = distStr.getDistanceDomain();
        this.signalDomain = signalDomain;
        this.reachabilityFunction = new ArrayList<>();
        this.reachabilityQueue = new LinkedList<>();
    }

    /**
     * @return reach's algorithm computation
     */
    public List<R> compute() {
        initReachableValues();
        reachCore();
        return selectMaxReachabilityValues();
    }

    private void initReachableValues() {
        for(int loc = 0; loc < model.size(); loc++) {
            Map<M, R> reachableValues = fetchInitialReachableValues(loc);
            reachabilityFunction.add(reachableValues);
        }
    }

    private Map<M, R> fetchInitialReachableValues(int location)
    {
        Map<M, R> reachableValues = new HashMap<>();
        R rightValue = rightSpatialSignal.apply(location);
        updateReachableValue(location,
                             distanceDomain.zero(),
                             rightValue,
                             reachableValues);
        return reachableValues;
    }

    private void reachCore() {
        while (!reachabilityQueue.isEmpty()) {
            Triple<Integer, M, R> t1 = reachabilityQueue.remove(0);
            int l1 = t1.getFirst();
            M d1 = t1.getSecond();
            R v1 = t1.getThird();
            updateReachability(l1, d1, v1);
        }
    }

    private void updateReachability(int l1, M d1, R v1) {
        for (Pair<Integer, E> neighbour: model.previous(l1)) {
            int l2 = neighbour.getFirst();
            M d2 = newDistance(d1, neighbour.getSecond());
            // Note: old condition was distanceDomain.lessOrEqual(d2, upperBound)
            // but I think this one is more correct
            if (distStr.isWithinBounds(d2)) {
                R value = signalDomain.conjunction(v1,
                                            leftSpatialSignal.apply(l2));
                combine(l2, d2, value);
            }
        }
    }

    private M newDistance(M d1, E weight) {
        return distanceDomain.sum(
                distStr.getDistanceFunction().apply(weight),
                d1);
    }

    private void combine(int location, M distance, R value) {
        Map<M, R> reachableValues = reachabilityFunction.get(location);
        if(reachableValues.containsKey(distance)) {
            R oldValue = reachableValues.get(distance);
            value = signalDomain.disjunction(value, oldValue);
            if (!signalDomain.equalTo(oldValue, value)) {
                updateReachableValue(location, distance, value, reachableValues);
            }
        } else {
            updateReachableValue(location, distance, value, reachableValues);
        }
    }

    private void updateReachableValue(int location,
                                      M distance,
                                      R newValue,
                                      Map<M, R> values)
    {
        values.put(distance, newValue);
        reachabilityQueue.add(new Triple<>(location, distance, newValue));
    }

    private List<R> selectMaxReachabilityValues() {
        return reachabilityFunction.stream()
                        .map(this::maximizeLocationValue)
                        .collect(Collectors.toCollection(ArrayList::new));
    }

    private R maximizeLocationValue(Map<M, R> reachabilityMap)
    {
        return reachabilityMap.entrySet()
                .stream()
                .filter(e -> distStr.isWithinBounds(e.getKey()))
                .map(Map.Entry::getValue)
                .reduce(signalDomain.min(), signalDomain::disjunction);
    }
}
