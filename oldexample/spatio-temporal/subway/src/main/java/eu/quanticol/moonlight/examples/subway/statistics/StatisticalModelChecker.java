package eu.quanticol.moonlight.examples.subway.statistics;

import eu.quanticol.moonlight.examples.subway.statistics.SignalStatistics.Statistics;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The StatisticalModelChecker is a class for evaluating a property over
 * multiple trajectories and determining the probability of satisfying it.
 *
 * @param <E> The Value Type of Spatial Model edges
 * @param <S> The Type of the Domain of the Input Signal
 * @param <T> The Output Type of the monitoring: Satisfiability | Robustness
 */
public class StatisticalModelChecker<E,S,T> {
    /**
     * Input Parameters
     */
    private final SpatialTemporalMonitor<E,S,T> monitor;
    private final Collection<? extends SpatialTemporalSignal<S>> samples;
    private final LocationService<E> locService;

    /**
     * Output Results
     */
    private final List<SpatialTemporalSignal<T>> results;
    private final SignalStatistics<SpatialTemporalSignal<T>> stats;

    /**
     * Instantiating a Statistical Model Checker requires the following:
     * @param propertyMonitor a monitor for the property of interest
     * @param trajectorySamples a Collection of trajectories to analyze
     * @param locationService a location service for the space model of interest
     */
    public StatisticalModelChecker(
            SpatialTemporalMonitor<E,S,T> propertyMonitor,
            Collection<? extends SpatialTemporalSignal<S>> trajectorySamples,
            LocationService<E> locationService
    ) {
        monitor = propertyMonitor;
        samples = trajectorySamples;
        locService = locationService;

        stats =  new SignalStatistics<SpatialTemporalSignal<T>>();
        results = new ArrayList<>();
    }

    /**
     * This is the actual execution of the statistical model checking,
     * by executing the chosen <code>monitor</code> over the provided
     * <code>samples</code>.
     *
     * This has the side effect of updating <code>results</code>
     * and <code>stats</code>.
     */
    public void compute() {
        for (SpatialTemporalSignal<S> s : samples) {

            SpatialTemporalSignal<T> result = stats.record(
                                        () -> monitor.monitor(locService, s));
            if (result != null)
                results.add(result);
        }
    }


    /**
     * @return The results collected, following a <code>compute</code> execution
     */
    public Statistics getStats() {
        return stats.analyze();
    }

    /**
     * @return The results collected, following a <code>compute</code> execution
     */
    public List<SpatialTemporalSignal<T>> getResults() {
        return results;
    }


}
