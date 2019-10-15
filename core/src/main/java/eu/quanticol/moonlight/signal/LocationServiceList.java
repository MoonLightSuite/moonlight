/**
 * 
 */
package eu.quanticol.moonlight.signal;

import java.util.Iterator;
import java.util.LinkedList;

import eu.quanticol.moonlight.util.Pair;

/**
 * @author loreti
 *
 */
public class LocationServiceList<V> implements LocationService<V> {
	
	private LinkedList<Pair<Double,SpatialModel<V>>> steps = new LinkedList<>();
	private Pair<Double,SpatialModel<V>> last;
	
	public LocationServiceList() {

	}
	
	public void add( double t, SpatialModel<V> m) {
		if ((last==null)||(last.getFirst()<t)) {
			last = new Pair<>(t,m);
			steps.add(last);
		} else {
			throw new IllegalArgumentException("Wrong time! Is "+t+" expexted >"+last.getFirst()+"!");
		}
	}

	@Override
	public SpatialModel<V> get(double t) {
		 Pair<Double,SpatialModel<V>> temp = null;
		 for (Pair<Double, SpatialModel<V>> p : steps) {
			if ((temp!=null)&&(t<temp.getFirst())) {
				return (temp!=null?temp.getSecond():null);
			}
			temp = p;
		}
		return (temp!=null?temp.getSecond():null);
	}

	@Override
	public Iterator<Pair<Double, SpatialModel<V>>> times() {
		return steps.iterator();
	}

	@Override
	public boolean isEmpty() {
		return steps.isEmpty();
	}

}
