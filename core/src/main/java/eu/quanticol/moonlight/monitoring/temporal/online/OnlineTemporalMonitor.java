package eu.quanticol.moonlight.monitoring.temporal.online;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.signal.ImmutableSegment;
import eu.quanticol.moonlight.signal.OnlineSignal;

import java.util.List;

public interface OnlineTemporalMonitor<T extends Comparable<T>,R extends Comparable<R>> {

    List<ImmutableSegment<AbstractInterval<R>>> monitor(List<ImmutableSegment<AbstractInterval<T>>> updates);

    OnlineSignal<R> getResult();

}
