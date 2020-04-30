package eu.quanticol.moonlight.examples.subway.statistics;

import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StatisticalModelChecker<E,S,T> {
    private SpatialTemporalMonitor<E,S,T> monitor;
    private Collection<SpatialTemporalSignal<S>> samples;
    private LocationService<E> locService;
    private List<Signal<T>> results;

    public StatisticalModelChecker(
            SpatialTemporalMonitor<E,S,T> propertyMonitor,
            Collection<SpatialTemporalSignal<S>> trajectorySamples,
            LocationService<E> locationService
    ) {
        monitor = propertyMonitor;
        samples = trajectorySamples;
        locService = locationService;

        results = new ArrayList<>();
    }

    public void run() {
        for(SpatialTemporalSignal<S> s : samples) {
            SpatialTemporalSignal<T> result = monitor.monitor(locService, s);
            results.add(result.getSignals().get(0));
        }
    }


}
