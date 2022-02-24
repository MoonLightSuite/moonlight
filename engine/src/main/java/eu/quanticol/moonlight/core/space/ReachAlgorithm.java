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
     * This reachability list is quite special: the i-th element of the list
     * denotes the set of optimal values for each distance from location i.
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
        initReachabilityMap();
        reachCore();
        return selectMaxReachabilityValues();
    }

    /**
     * initializes the list of the best reachable values
     */
    private void initReachabilityMap() {
        for(int loc = 0; loc < model.size(); loc++) {
            Map<M, R> reachabilityMap = fetchInitialReachabilityMap(loc);
            reachabilityFunction.add(reachabilityMap);
        }
    }

    private Map<M, R> fetchInitialReachabilityMap(int location)
    {
        Map<M, R> reachabilityMap = new HashMap<>();
        R rightValue = rightSpatialSignal.apply(location);
        reachabilityMap.put(distanceDomain.zero(), rightValue);
        reachabilityQueue.add(new Triple<>(location,
                                           distanceDomain.zero(),
                                           rightValue));
        return reachabilityMap;
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

    private void updateReachability(int l1, M d1, R v1)
    {
        for (Pair<Integer, E> neighbour: model.previous(l1)) {
            int l2 = neighbour.getFirst();
            M d2 = newDistance(d1, neighbour.getSecond());
            // Note: old condition was distanceDomain.lessOrEqual(d2, upperBound)
            // but I think this one is more correct
            if (distStr.isWithinBounds(d2)) {
                Triple<Integer, M, R> t2 = combine(l2, d2,
                        signalDomain.conjunction(v1, leftSpatialSignal.apply(l2)),
                        reachabilityFunction.get(l2));
                if (t2 != null) {
                    reachabilityQueue.add(t2);
                }
            }
        }
    }

    private M newDistance(M d1, E weight) {
        return distanceDomain.sum(
                distStr.getDistanceFunction().apply(weight),
                d1);
    }

    private Triple<Integer, M, R> combine(int l, M d, R v, Map<M, R> fr) {
        R v1 = fr.get(d);
        if (v1 != null) {
            R v2 = signalDomain.disjunction(v, v1);
            if (!signalDomain.equalTo(v1,v2)) {
                fr.put(d, v2);
                return new Triple<>(l, d, v2);
            }
        } else {
            Triple<Integer, M, R> t = new Triple<>(l, d, v);
            fr.put(d, v);
            return t;
        }
        return null;
    }

    private List<R> selectMaxReachabilityValues()
    {
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
