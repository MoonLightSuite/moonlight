package eu.quanticol.moonlight.monitoring.online;

import eu.quanticol.moonlight.signal.online.SignalInterface;
import eu.quanticol.moonlight.signal.online.Update;

import java.io.Serializable;
import java.util.List;

public interface OnlineMonitor
<T extends Comparable<T> & Serializable,
 V extends Comparable<V>,
 R extends Comparable<R>>
{
    List<Update<T, R>> monitor(Update<T, V> signalUpdate);

    SignalInterface<T, R> getResult();
}
