package eu.quanticol.moonlight.online.signal;

import java.io.Serializable;
import java.util.List;

/**
 * General interface that represents a Signal used by the monitoring processes
 *
 * @param <T> The time domain of interest, typically a {@link Number}
 * @param <V> The signal domain to be considered
 *
 * @see OnlineSpaceTimeSignal for a concrete implementation
 * @see MultiOnlineSpaceTimeSignal for a concrete implementation
 */
public interface SpaceTimeSignal<T extends Comparable<T> & Serializable, V>
        extends TimeSignal<T, List<V>>
{
    /**
     * Returns the size of the spatial universe of reference
     *
     * @return the size of the spatial universe of reference
     */
    int getSize();
}
