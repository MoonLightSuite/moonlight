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

    public List<R> compute() {
        List<Map<M, R>> reachFunc = new ArrayList<>();
        List<Triple<Integer, M, R>> queue = new LinkedList<>();

        initReach(reachFunc, queue);
        reachCore(queue, reachFunc);

        return collectReachValue(reachFunc);
    }

    private void initReach(List<Map<M, R>> reachFunc,
                           List<Triple<Integer, M, R>> queue)
    {
        for(int i = 0; i < model.size(); i++) {
            Map<M, R> map = new HashMap<>();
            queue.add(new Triple<>(i, distanceDomain.zero(), rightSpatialSignal.apply(i)));
            map.put(distanceDomain.zero(), rightSpatialSignal.apply(i));
            reachFunc.add(map);
        }
    }

    private void reachCore(List<Triple<Integer, M, R>> queue,
                               List<Map<M, R>> reachFunc)
    {
        while (!queue.isEmpty()) {
            Triple<Integer, M, R> t1 = queue.remove(0);
            int l1 = t1.getFirst();
            M d1 = t1.getSecond();
            R v1 = t1.getThird();
            for (Pair<Integer, E> pre : model.previous(l1)) {
                int l2 = pre.getFirst();
                M d2 = distanceDomain.sum(
                        distStr.getDistanceFunction().apply(pre.getSecond()),
                                                            d1);
                // Note: old condition was distanceDomain.lessOrEqual(d2, upperBound)
                // but I think this one is more correct
                if (distStr.isWithinBounds(d2)) {
                    Triple<Integer, M, R> t2 = combine(signalDomain, l2, d2,
                            signalDomain.conjunction(v1, leftSpatialSignal.apply(l2)), reachFunc.get(l2));
                    if (t2 != null) {
                        queue.add(t2);
                    }
                }
            }
        }
    }

    public static <A, R> Triple<Integer, A, R> combine(SignalDomain<R> mDomain,
                                                       int l,
                                                       A d,
                                                       R v,
                                                       Map<A, R> fr)
    {
        R v1 = fr.get(d);
        if (v1 != null) {
            R v2 = mDomain.disjunction(v, v1);
            if (!mDomain.equalTo(v1,v2)) {
                fr.put(d, v2);
                return new Triple<>(l, d, v2);
            }
        } else {
            Triple<Integer, A, R> t = new Triple<>(l, d, v);
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
