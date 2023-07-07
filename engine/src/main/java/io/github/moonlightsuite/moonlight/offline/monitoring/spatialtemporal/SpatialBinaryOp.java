package io.github.moonlightsuite.moonlight.offline.monitoring.spatialtemporal;

import io.github.moonlightsuite.moonlight.core.signal.SignalDomain;
import io.github.moonlightsuite.moonlight.core.space.DistanceStructure;
import io.github.moonlightsuite.moonlight.core.space.LocationService;
import io.github.moonlightsuite.moonlight.core.space.SpaceIterator;
import io.github.moonlightsuite.moonlight.core.space.SpatialModel;
import io.github.moonlightsuite.moonlight.offline.signal.ParallelSignalCursor;
import io.github.moonlightsuite.moonlight.offline.signal.SpatialTemporalSignal;

import java.util.function.Function;
import java.util.function.IntFunction;

import static io.github.moonlightsuite.moonlight.core.algorithms.SpatialAlgorithms.reach;
import static io.github.moonlightsuite.moonlight.offline.signal.SignalCursor.isNotCompleted;

public class SpatialBinaryOp<S, R> {
    private final SpaceIterator<Double, S> spaceItr;
    private final SignalDomain<R> domain;
    private SpatialTemporalSignal<R> result;

    public SpatialBinaryOp(
            LocationService<Double, S> l,
            Function<SpatialModel<S>, DistanceStructure<S, ?>> distance,
            SignalDomain<R> domain) {
        this.domain = domain;
        spaceItr = new SpaceIterator<>(l, distance);
    }

    public SpatialTemporalSignal<R> computeReach(SpatialTemporalSignal<R> s1,
                                                 SpatialTemporalSignal<R> s2) {
        outputInit(s1.getNumberOfLocations());
        if (!spaceItr.isLocationServiceEmpty()) {
            doCompute(s1, s2);
        }
        return result;
    }

    private void outputInit(int locations) {
        result = new SpatialTemporalSignal<>(locations);
    }

    public SpatialTemporalSignal<R> doCompute(
            SpatialTemporalSignal<R> s1,
            SpatialTemporalSignal<R> s2) {
        outputInit(s1.getNumberOfLocations());
        if (!spaceItr.isLocationServiceEmpty()) {
            ParallelSignalCursor<R> c1 = s1.getSignalCursor(true);
            ParallelSignalCursor<R> c2 = s2.getSignalCursor(true);
            double t = Math.max(s1.start(), s2.start());

            spaceItr.init(t);
            c1.move(t);
            c2.move(t);

            //Loop invariant: (current.getFirst() <= time) &&
            //                ((next == null) || (time < next.getFirst()))
            while (!Double.isNaN(t) && isNotCompleted(c1, c2)) {
                var ds = spaceItr.generateDistanceStructure();
                var spatialSignal1 = c1.getCurrentValue();
                var spatialSignal2 = c2.getCurrentValue();

                result.add(t, reach(domain, spatialSignal1, spatialSignal2,
                        ds));

                t = getTNext(domain, c1, c2, spatialSignal1, spatialSignal2);
                if (spaceItr.isNextSpaceModelMeaningful()) {
                    spaceItr.shiftSpatialModel();
                }
            }
        }
        return result;
    }

    private double getTNext(SignalDomain<R> domain,
                            ParallelSignalCursor<R> c1,
                            ParallelSignalCursor<R> c2,
                            IntFunction<R> spatialSignal1,
                            IntFunction<R> spatialSignal2) {
        double tNext = Math.min(c1.nextTime(), c2.nextTime());
        c1.move(tNext);
        c2.move(tNext);
        spaceItr.forEach(tNext, (itT, itDs) -> {
            //result.add(t, escape(domain, values, f));
            var output = reach(domain, spatialSignal1, spatialSignal2, itDs);
            result.add(itT, output);
        });
        return tNext;
    }
}
