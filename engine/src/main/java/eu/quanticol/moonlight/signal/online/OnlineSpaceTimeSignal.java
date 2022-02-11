package eu.quanticol.moonlight.signal.online;

import eu.quanticol.moonlight.algorithms.online.Signals;
import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.SignalDomain;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OnlineSpaceTimeSignal<D extends Comparable<D>>
        implements SpaceTimeSignal<Double, AbstractInterval<D>>
{
    private final TimeChain<Double, List<AbstractInterval<D>>> segments;
    private final int size;

    public OnlineSpaceTimeSignal(int locations,
                                 SignalDomain<D> domain)
    {
        List<AbstractInterval<D>> any =
                IntStream.range(0, locations).boxed()
                        .map(i -> new AbstractInterval<>(domain.min(),
                                                         domain.max()))
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
    public boolean refine(Update<Double, List<AbstractInterval<D>>> u) {
        return Signals.refine(segments, u,
                (v, vNew) -> IntStream.range(0, size)
                        .filter(i -> v.get(i).contains(vNew.get(i)))
                        .count() != 0
        );
    }

    @Override
    public boolean refine(
            TimeChain<Double, List<AbstractInterval<D>>> updates)
    {
        return Signals.refineChain(segments, updates,
                (v, vNew) -> IntStream.range(0, size)
                        .filter(i -> v.get(i).contains(vNew.get(i)))
                        .count() != 0
        );
    }

    /**
     * Returns the internal chain of segments.
     *
     * @return the total chain of segments of the signal
     * @throws UnsupportedOperationException when not allowed by implementors
     */
    @Override
    public TimeChain<Double, List<AbstractInterval<D>>> getSegments() {
        return segments;
    }

    /**
     * Temporal projection operation that selects a sub-part of the signal
     * delimited by the time instants provided by the input parameters.
     *
     * @param from beginning of the time frame of interest
     * @param to   ending of the time frame of interest
     * @return the chain of segments of the signal delimited by the input
     * @throws UnsupportedOperationException when not allowed by implementors
     */
    @Override
    public TimeChain<Double, List<AbstractInterval<D>>> select(Double from,
                                                               Double to)
    {
        return Signals.select(segments, from, to);
    }

    /**
     * Returns the size of the spatial universe of reference
     *
     * @return the size of the spatial universe of reference
     */
    @Override
    public int getSize() {
        return size;
    }
}
