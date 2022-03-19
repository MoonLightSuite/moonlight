package eu.quanticol.moonlight.online.signal;

import eu.quanticol.moonlight.core.signal.Sample;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Immutable concrete implementation of {@link Sample}.
 * Note that for space efficiency reasons the segment does not have an ending,
 * as this would be a replication of data with the contiguous segment.
 *
 * Conversely, chains of segments, or {@link TimeChain}, might have an ending.
 *
 * @param <T> The time-domain of the Segment
 * @param <V> The value-domain of the Segment
 *
 * @see Sample
 * @see TimeChain
 */
public class TimeSegment
        <T extends Comparable<T>, V>
        implements Sample<T, V>
{
    private final T start;
    private final V value;

    public TimeSegment(@NotNull T start, @NotNull V value) {
        this.start = start;
        this.value = value;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public T getStart() {
        return start;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSegment)) return false;
        TimeSegment<?, ?> that = (TimeSegment<?, ?>) o;
        return Objects.equals(start, that.start) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, value);
    }

    @Override
    public String toString() {
        return "Segment(" + "start=" + start + ", value=" + value + ')';
    }
}
