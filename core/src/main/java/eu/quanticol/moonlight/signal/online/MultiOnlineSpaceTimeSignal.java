package eu.quanticol.moonlight.signal.online;

import eu.quanticol.moonlight.algorithms.online.Refinement;
import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.SignalDomain;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MultiOnlineSpaceTimeSignal
        implements TimeSignal<Double, List<List<AbstractInterval<?>>>>
{
    private final TimeChain<Double, List<List<AbstractInterval<?>>>> segments;
    private final int size;


    public MultiOnlineSpaceTimeSignal(int locations,
                              SignalDomain<List<AbstractInterval<?>>> domain)
    {
        List<List<AbstractInterval<?>>> any =
                IntStream.range(0, locations).boxed()
                         .map(i -> domain.any())
                         .collect(Collectors.toList());

        segments = new TimeChain<>(Double.POSITIVE_INFINITY);
        segments.add(new TimeSegment<>(0.0, any));

        size = locations;
    }

    /**
     * Performs an update of the internal representation of the signal,
     * given the data available in the update.
     *
     * @param u the new data available from new knowledge
     * @return <code>true</code> if the refinement actually updates the signal.
     * <code>false</code> otherwise
     */
    @Override
    public boolean refine(Update<Double, List<List<AbstractInterval<?>>>> u) {
        BiPredicate<List<AbstractInterval<?>>, List<AbstractInterval<?>>> pred =
                (v, vNew) -> IntStream.range(0, v.size())   //.parallel()
                        .filter(i -> !v.get(i)
                                .contains(vNew.get(i)))
                        .count() != 0;

        return Refinement.refine(segments, u,
                (v, vNew) -> IntStream.range(0, size)   //.parallel()
                                      .filter(i -> pred.test(v.get(i),
                                                             vNew.get(i)))
                                      .count() != 0
                );
    }

    /**
     * Temporal projection operation that selects a sub-part of the signal
     * delimited by the time instants provided by the input parameters.
     *
     * @param start beginning of the time frame of interest
     * @param end   ending of the time frame of interest
     * @return the chain of segments of the signal delimited by the input
     */
    @Override
    public TimeChain<Double, List<List<AbstractInterval<?>>>> select(
            Double start, Double end)
    {
        throw new UnsupportedOperationException("Not allowed for SpaceTime" +
                                                " Signals");
    }

    /**
     * Returns the internal chain of segments.
     *
     * @return the total chain of segments of the signal
     * @throws UnsupportedOperationException when not allowed by implementors
     */
    @Override
    public TimeChain<Double, List<List<AbstractInterval<?>>>> getSegments() {
        return segments;
    }
}
