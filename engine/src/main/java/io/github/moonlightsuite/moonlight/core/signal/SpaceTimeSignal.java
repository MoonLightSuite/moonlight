package io.github.moonlightsuite.moonlight.core.signal;

import java.util.List;

/**
 * General interface that represents a Signal used by the monitoring processes
 *
 * @param <T> The time domain of interest, typically a {@link Number}
 * @param <V> The signal domain to be considered
 */
public interface SpaceTimeSignal<T extends Comparable<T>, V>
        extends TimeSignal<T, List<V>>
{
    /**
     * @return the size of the spatial universe of reference
     */
    int getSize();
}