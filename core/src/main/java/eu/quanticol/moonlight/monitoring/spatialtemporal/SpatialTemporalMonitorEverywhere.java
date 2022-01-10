/**
 * 
 */
package eu.quanticol.moonlight.monitoring.spatialtemporal;

import java.util.Iterator;
import java.util.function.Function;

import eu.quanticol.moonlight.formula.SignalDomain;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.ParallelSignalCursor;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.util.Pair;

/**
 * @author loreti
 *
 */
public class SpatialTemporalMonitorEverywhere<E,S,T> implements SpatialTemporalMonitor<E, S, T> {

	private SpatialTemporalMonitor<E, S, T> m;
	private Function<SpatialModel<E>, DistanceStructure<E, ?>> distance;
	private SignalDomain<T> domain;

	public SpatialTemporalMonitorEverywhere(SpatialTemporalMonitor<E, S, T> m,
                                            Function<SpatialModel<E>, DistanceStructure<E, ?>> distance, SignalDomain<T> domain) {
		this.m = m;
		this.distance = distance;
		this.domain = domain;
	}

	@Override
	public SpatialTemporalSignal<T> monitor(LocationService<E> locationService, SpatialTemporalSignal<S> signal) {
		return computeEverywhereDynamic(locationService,m.monitor(locationService, signal));
	}

    private SpatialTemporalSignal<T> computeEverywhereDynamic(
            LocationService<E> l, SpatialTemporalSignal<T> s) {
        SpatialTemporalSignal<T> toReturn = new SpatialTemporalSignal<T>(s.getNumberOfLocations());
        if (l.isEmpty()) {
            return toReturn;
        }

        ParallelSignalCursor<T> cursor = s.getSignalCursor(true);

        Iterator<Pair<Double, SpatialModel<E>>> locationServiceIterator = l.times();
        Pair<Double, SpatialModel<E>> current = locationServiceIterator.next();
        Pair<Double, SpatialModel<E>> next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
        double time = cursor.getTime();
        while ((next != null)&&(next.getFirst()<=time)) {
            current = next;
            next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
        }

        //Loop invariant: (current.getFirst()<=time)&&((next==null)||(time<next.getFirst()))
        SpatialModel<E> sm = current.getSecond();
        DistanceStructure<E, ?> f = distance.apply(sm);
        while (!cursor.completed() && !Double.isNaN(time)) {
            Function<Integer, T> spatialSignal = cursor.getValue();
            toReturn.add(time, f.everywhere(domain, spatialSignal));
            double nextTime = cursor.forward();
            while ((next != null)&&(next.getFirst()<nextTime)) {
                current = next;
                time = current.getFirst();
                next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
                f = distance.apply(current.getSecond());
                toReturn.add(time, f.everywhere(domain, spatialSignal));
            }
            time = nextTime;
            if ((next!=null)&&(next.getFirst()==time)) {
                current = next;
                f = distance.apply(current.getSecond());
                next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
            }
        }

        //TODO: Manage end of signal!
        return toReturn;
    }
}
