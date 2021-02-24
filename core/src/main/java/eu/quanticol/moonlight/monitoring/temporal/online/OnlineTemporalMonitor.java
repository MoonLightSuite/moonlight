package eu.quanticol.moonlight.monitoring.temporal.online;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.online.ImmutableSegment;
import eu.quanticol.moonlight.signal.online.SignalInterface;

import java.util.List;

public interface OnlineTemporalMonitor
<V extends Comparable<V>, R extends Comparable<R>> extends TemporalMonitor<V, R>
{

    @Override
    default Signal<R> monitor(Signal<V> signal) {
        throw new UnsupportedOperationException("Offline monitoring is not " +
                                                "supported yet!");
    }

    List<ImmutableSegment<AbstractInterval<R>>> monitor(List<ImmutableSegment<AbstractInterval<V>>> updates);

    SignalInterface<Double, AbstractInterval<R>> getResult();


}
