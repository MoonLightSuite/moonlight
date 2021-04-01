package eu.quanticol.moonlight.monitoring.online.strategy.time;

import eu.quanticol.moonlight.signal.online.TimeSignal;
import eu.quanticol.moonlight.signal.online.Update;

import java.io.Serializable;
import java.util.List;

public interface OnlineMonitor<T extends Comparable<T> & Serializable, V, R>
{
    List<Update<T, R>> monitor(Update<T, V> signalUpdate);

    TimeSignal<T, R> getResult();
}
