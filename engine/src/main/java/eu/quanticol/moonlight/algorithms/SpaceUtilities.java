package eu.quanticol.moonlight.algorithms;

import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.core.space.DefaultDistanceStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpaceUtilities {
    private SpaceUtilities() {}



    public static <T, A, R> List<R> everywhere(SignalDomain<R> dModule,
                                               IntFunction<R> s,
                                               DefaultDistanceStructure<T, A> ds)
    {
        ArrayList<R> values = dModule.createArray(ds.getModel().size());
        for (int i = 0; i < ds.getModel().size(); i++) {
            R v = dModule.max();
            for (int j = 0; j < ds.getModel().size(); j++) {
                if (ds.areWithinBounds(i, j)) {
                    v = dModule.conjunction(v, s.apply(j));
                }
            }
            values.set(i, v);
        }
        return values;
    }

    public static <T, A, R> List<R> somewhere(SignalDomain<R> dModule,
                                              IntFunction<R> s,
                                              DefaultDistanceStructure<T, A> ds)
    {
        ArrayList<R> values = dModule.createArray(ds.getModel().size());
        for (int i = 0; i < ds.getModel().size(); i++) {
            R v = dModule.min();
            for (int j = 0; j < ds.getModel().size(); j++) {
                if (ds.areWithinBounds(i, j)) {
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
            DefaultDistanceStructure<T, A> ds)
    {
        return IntStream
                .range(0, ds.getModel().size())
                .boxed()
                .parallel()
                .map(i -> {
                    R v = dModule.min();
                    for (int j = 0; j < ds.getModel().size(); j++) {
                        if (ds.areWithinBounds(i, j)) {
                            v = dModule.disjunction(v, s.apply(j));
                        }
                    }
                    return v;
                }).collect(Collectors.toList());
    }

    public static <T, A, R> List<R> everywhereParallel(
            SignalDomain<R> dModule,
            IntFunction<R> s,
            DefaultDistanceStructure<T, A> ds)
    {
        return IntStream
                .range(0, ds.getModel().size())
                .boxed()
                .parallel()
                .map(i -> {
                    R v = dModule.max();
                    for (int j = 0; j < ds.getModel().size(); j++) {
                        if (ds.areWithinBounds(i, j)) {
                            v = dModule.conjunction(v, s.apply(j));
                        }
                    }
                    return v;
                }).collect(Collectors.toList());
    }


}
