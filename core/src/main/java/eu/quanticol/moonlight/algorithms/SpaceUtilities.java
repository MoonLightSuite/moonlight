package eu.quanticol.moonlight.algorithms;

import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.space.DistanceStructure;
import eu.quanticol.moonlight.util.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpaceUtilities {
    private SpaceUtilities() {}

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

    public static <T, A, R> List<R> everywhere(SignalDomain<R> dModule,
                                               IntFunction<R> s,
                                               DistanceStructure<T, A> ds)
    {
        ArrayList<R> values = dModule.createArray(ds.getModelSize());
        for (int i = 0; i < ds.getModelSize(); i++) {
            R v = dModule.max();
            for (int j = 0; j < ds.getModelSize(); j++) {
                if (ds.checkDistance(i, j)) {
                    v = dModule.conjunction(v, s.apply(j));
                }
            }
            values.set(i, v);
        }
        return values;
    }

    public static <T, A, R> List<R> somewhere(SignalDomain<R> dModule,
                                              IntFunction<R> s,
                                              DistanceStructure<T, A> ds)
    {
        ArrayList<R> values = dModule.createArray(ds.getModelSize());
        for (int i = 0; i < ds.getModelSize(); i++) {
            R v = dModule.min();
            for (int j = 0; j < ds.getModelSize(); j++) {
                if (ds.checkDistance(i, j)) {
                    v = dModule.disjunction(v, s.apply(j));
                }
            }
            values.set(i, v);
        }
        return values;
    }

    public static <T, A, R> List<R> somewhereParallel(
            SignalDomain<R> dModule,
            IntFunction<R> s,
            DistanceStructure<T, A> ds)
    {
        return IntStream
                .range(0, ds.getModelSize())
                .boxed()
                .parallel()
                .map(i -> {
                    R v = dModule.min();
                    for (int j = 0; j < ds.getModelSize(); j++) {
                        if (ds.checkDistance(i, j)) {
                            v = dModule.disjunction(v, s.apply(j));
                        }
                    }
                    return v;
                }).collect(Collectors.toList());
    }

    public static <T, A, R> List<R> everywhereParallel(
            SignalDomain<R> dModule,
            IntFunction<R> s,
            DistanceStructure<T, A> ds)
    {
        return IntStream
                .range(0, ds.getModelSize())
                .boxed()
                .parallel()
                .map(i -> {
                    R v = dModule.max();
                    for (int j = 0; j < ds.getModelSize(); j++) {
                        if (ds.checkDistance(i, j)) {
                            v = dModule.conjunction(v, s.apply(j));
                        }
                    }
                    return v;
                }).collect(Collectors.toList());
    }


}
