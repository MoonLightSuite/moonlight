package eu.quanticol.moonlight.online.signal;

import eu.quanticol.moonlight.core.base.Box;
import eu.quanticol.moonlight.core.signal.TimeSignal;
import eu.quanticol.moonlight.online.algorithms.Signals;
import eu.quanticol.moonlight.core.signal.SignalDomain;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MultiOnlineSpaceTimeSignal
        implements TimeSignal<Double, List<List<Box<?>>>>
{
    private final TimeChain<Double, List<List<Box<?>>>> segments;
    private final int size;


    public MultiOnlineSpaceTimeSignal(int locations,
                              SignalDomain<List<Box<?>>> domain)
    {
        List<List<Box<?>>> any =
                IntStream.range(0, locations).boxed()
                         .map(i -> domain.any())
                         .collect(Collectors.toList());

        segments = new TimeChain<>(new TimeSegment<>(0.0, any), Double.POSITIVE_INFINITY);

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
    public boolean refine(Update<Double, List<List<Box<?>>>> u) {
        return Signals.refine(segments, u,
                (v, vNew) -> IntStream.range(0, size)
                                      .filter(i -> containment(v.get(i),
                                                               vNew.get(i)))
                                      .count() != 0
                );
    }

    @Override
    public boolean refine(
            TimeChain<Double, List<List<Box<?>>>> updates)
    {
        return Signals.refineChain(segments, updates,
                (v, vNew) -> IntStream.range(0, size)   //.parallel()
                                      .filter(i -> containment(v.get(i),
                                                               vNew.get(i)))
                                      .count() != 0
                );
    }

    private static boolean containment(List<Box<?>> v,
                                       List<Box<?>> vNew)
    {
        return  IntStream.range(0, v.size())
                         .filter(i -> !v.get(i).contains(vNew.get(i)))
                         .count() != 0;
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
    public TimeChain<Double, List<List<Box<?>>>> select(
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
    public TimeChain<Double, List<List<Box<?>>>> getSegments() {
        return segments;
    }
}
