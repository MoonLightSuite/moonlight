/**
 * 
 */
package eu.quanticol.moonlight.monitoring.spatiotemporal;

import java.util.Iterator;
import java.util.function.Function;

import eu.quanticol.moonlight.formula.SignalDomain;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.ParallelSignalCursor;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;
import eu.quanticol.moonlight.util.Pair;

/**
 * @author loreti
 *
 */
public class SpatioTemporalMonitorEscape<E,S,T> implements SpatioTemporalMonitor<E, S, T> {

	private SpatioTemporalMonitor<E, S, T> m;
	private Function<SpatialModel<E>, DistanceStructure<E, ?>> distance;
	private SignalDomain<T> domain;

	public SpatioTemporalMonitorEscape(SpatioTemporalMonitor<E, S, T> m,
			Function<SpatialModel<E>, DistanceStructure<E, ?>> distance, SignalDomain<T> domain) {
		this.m = m;
		this.distance = distance;
		this.domain = domain;
	}

	@Override
	public SpatioTemporalSignal<T> monitor(LocationService<E> locationService, SpatioTemporalSignal<S> signal) {
		return computeEscapeDynamic(locationService, m.monitor(locationService, signal));
	}

    private SpatioTemporalSignal<T> computeEscapeDynamic(LocationService<E> l,
            SpatioTemporalSignal<T> s) {
    	
    	SpatioTemporalSignal<T> toReturn = new SpatioTemporalSignal<T>(s.getNumberOfLocations());
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
    	while (!cursor.completed() && !Double.isNaN(time)) {
    		Function<Integer, T> spatialSignal = cursor.getValue();
    		SpatialModel<E> sm = current.getSecond();
    		DistanceStructure<E, ?> f = distance.apply(sm);
    		toReturn.add(time, f.escape(domain, spatialSignal));
    		double nextTime = cursor.forward();
    		while ((next != null)&&(next.getFirst()<nextTime)) {
    			current = next;	
    			time = current.getFirst();
    			next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
    			f = distance.apply(current.getSecond());
    			toReturn.add(time, f.escape(domain, spatialSignal));
    		}
    		time = nextTime;
            current = (next!=null?next:current);
            next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
    	}
    	//TODO: Manage end of signal!
    	return toReturn;
    }	
	
}
