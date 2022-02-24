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
    }

    /**
     * @return reach's algorithm computation
     */
    public List<R> compute() {
        List<Map<M, R>> reachFunc = new ArrayList<>();
        List<Triple<Integer, M, R>> queue = initReach(reachFunc);
        reachCore(queue, reachFunc);
        return collectReachValue(reachFunc);
    }

    private List<Triple<Integer, M, R>> initReach(List<Map<M, R>> reachFunc) {
        List<Triple<Integer, M, R>> bestReachableValues = new LinkedList<>();
        for(int loc = 0; loc < model.size(); loc++) {
            Map<M, R> reachabilityMap = initReachabilityMap(loc, bestReachableValues);
            reachFunc.add(reachabilityMap);
        }
        return bestReachableValues;
    }

    private Map<M, R> initReachabilityMap(
            int location,
            List<Triple<Integer, M, R>> bestReachableValues)
    {
        Map<M, R> reachabilityMap = new HashMap<>();
        R rightValue = rightSpatialSignal.apply(location);
        reachabilityMap.put(distanceDomain.zero(), rightValue);

        bestReachableValues.add(new Triple<>(location,
                                             distanceDomain.zero(),
                                             rightValue));

        return reachabilityMap;
    }

    private void reachCore(List<Triple<Integer, M, R>> bestReachableValues,
                               List<Map<M, R>> reachFunc)
    {
        while (!bestReachableValues.isEmpty()) {
            Triple<Integer, M, R> t1 = bestReachableValues.remove(0);
            int l1 = t1.getFirst();
            M d1 = t1.getSecond();
            R v1 = t1.getThird();
            for (Pair<Integer, E> pre: model.previous(l1)) {
                int l2 = pre.getFirst();
                M d2 = distanceDomain.sum(
                        distStr.getDistanceFunction().apply(pre.getSecond()),
                                                            d1);
                // Note: old condition was distanceDomain.lessOrEqual(d2, upperBound)
                // but I think this one is more correct
                if (distStr.isWithinBounds(d2)) {
                    Triple<Integer, M, R> t2 = combine(l2, d2,
                            signalDomain.conjunction(v1, leftSpatialSignal.apply(l2)),
                                                     reachFunc.get(l2));
                    if (t2 != null) {
                        bestReachableValues.add(t2);
                    }
                }
            }
        }
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

    private List<R> collectReachValue(List<Map<M, R>> reachFunction)
    {
        return reachFunction.stream()
                .map(rf -> computeReachValue(signalDomain, rf))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private R computeReachValue(SignalDomain<R> mDomain, Map<M, R> rf)
    {
        return rf.entrySet()
                .stream()
                .filter(e -> distStr.isWithinBounds(e.getKey()))
                .map(Map.Entry::getValue)
                .reduce(mDomain.min(), mDomain::disjunction);
    }
}
