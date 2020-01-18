/**
 * 
 */
package eu.quanticol.moonlight.signal;

import java.util.Iterator;

import eu.quanticol.moonlight.util.Pair;

/**
 * @author loreti
 *
 */
public interface LocationService<V> {
	
	public SpatialModel<V> get(double t);
	
	public Iterator<Pair<Double, SpatialModel<V>>> times();

	public boolean isEmpty();

	public static LocationService<Record> buildLocationService(int locations, RecordHandler edgeRecordHandler, double[] locationTimeArray,
			Object[][][][] graph) {
		LocationServiceList<Record> toReturn = new LocationServiceList<>();
		for( int i=0 ; i<locationTimeArray.length ; i++ ) {
			toReturn.add(locationTimeArray[i], SpatialModel.buildSpatialModel(locations,edgeRecordHandler,graph[i]));
		}
		return toReturn;
	}

}
