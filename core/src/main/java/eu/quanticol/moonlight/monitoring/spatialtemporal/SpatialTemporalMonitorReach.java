/**
 * 
 */
package eu.quanticol.moonlight.monitoring.spatialtemporal;

import java.util.ArrayList;
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
public class SpatialTemporalMonitorReach<E,S,T> implements SpatialTemporalMonitor<E, S, T> {

	private SpatialTemporalMonitor<E, S, T> m1;
	private Function<SpatialModel<E>, DistanceStructure<E, ?>> distance;
	private SpatialTemporalMonitor<E, S, T> m2;
	private SignalDomain<T> domain;

	public SpatialTemporalMonitorReach(SpatialTemporalMonitor<E, S, T> m1,
                                       Function<SpatialModel<E>, DistanceStructure<E, ?>> distance, SpatialTemporalMonitor<E, S, T> m2,
                                       SignalDomain<T> domain) {
		this.m1 = m1;
		this.distance = distance;
		this.m2 = m2;
		this.domain = domain;
	}

	@Override
	public SpatialTemporalSignal<T> monitor(LocationService<E> locationService, SpatialTemporalSignal<S> signal) {
		return computeReachDynamic(locationService, m1.monitor(locationService, signal), m2.monitor(locationService, signal));
	}
	
	private SpatialTemporalSignal<T> computeReachDynamic(LocationService<E> locationService, SpatialTemporalSignal<T> s1, SpatialTemporalSignal<T> s2) {
        SpatialTemporalSignal<T> toReturn = new SpatialTemporalSignal<T>(s1.getNumberOfLocations());
        if (locationService.isEmpty()) {
            return toReturn;
        }
        ParallelSignalCursor<T> c1 = s1.getSignalCursor(true);
        ParallelSignalCursor<T> c2 = s2.getSignalCursor(true);
        Iterator<Pair<Double, SpatialModel<E>>> locationServiceIterator = locationService.times();
        Pair<Double, SpatialModel<E>> current = locationServiceIterator.next();
        Pair<Double, SpatialModel<E>> next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
        double time = Math.max(s1.start(), s2.start());
        while ((next != null)&&(next.getFirst()<=time)) {
            current = next;
            next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
        }
        //Loop invariant: (current.getFirst()<=time)&&((next==null)||(time<next.getFirst()))
        c1.move(time);
        c2.move(time);
        SpatialModel<E> sm = current.getSecond();
        DistanceStructure<E, ?> f = distance.apply(sm);
        while (!c1.completed() && !c2.completed() && !Double.isNaN(time)) {
            Function<Integer, T> spatialSignal1 = c1.getValue();
            Function<Integer, T> spatialSignal2 = c2.getValue();
            ArrayList<T> values =  f.reach(domain, spatialSignal1, spatialSignal2);
            toReturn.add(time, (values::get));
            double nextTime = Math.min(c1.nextTime(), c2.nextTime());
            c1.move(nextTime);
            c2.move(nextTime);
            while ((next != null)&&(next.getFirst()<nextTime)) {
                current = next;
                time = current.getFirst();
                next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
                f = distance.apply(current.getSecond());
                values =  f.reach(domain, spatialSignal1, spatialSignal2);
                toReturn.add(time, f.escape(domain,(values::get)));
            }
            time = nextTime;
            if ((next!=null)&&(next.getFirst()==time)) {
                current = next;
                f = distance.apply(current.getSecond());
                next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
            }
        }
        return toReturn;
	}

	
}
