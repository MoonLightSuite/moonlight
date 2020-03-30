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
	
	SpatialModel<V> get(double t);
	
	Iterator<Pair<Double, SpatialModel<V>>> times();

	boolean isEmpty();

	static LocationService<Record> buildLocationServiceFromAdjacencyMatrix(int locations, RecordHandler edgeRecordHandler, double[] locationTimeArray,
																		   String[][][][] graph) {
		LocationServiceList<Record> toReturn = new LocationServiceList<>();
		for( int i=0 ; i<locationTimeArray.length ; i++ ) {
			toReturn.add(locationTimeArray[i], SpatialModel.buildSpatialModelFromAdjacencyMatrix(locations,edgeRecordHandler,graph[i]));
		}
		return toReturn;
	}

	static LocationService<Record> buildLocationServiceFromAdjacencyMatrix(int locations, RecordHandler edgeRecordHandler, double time,
																		   String[][][] graph) {
		LocationServiceList<Record> toReturn = new LocationServiceList<>();
		toReturn.add(time, SpatialModel.buildSpatialModelFromAdjacencyMatrix(locations,edgeRecordHandler,graph));
		return toReturn;
	}

	static LocationService<Record> buildLocationServiceFromAdjacencyMatrix(int locations, RecordHandler edgeRecordHandler, double[] locationTimeArray,
																		   double[][][][] graph) {
		LocationServiceList<Record> toReturn = new LocationServiceList<>();
		for( int i=0 ; i<locationTimeArray.length ; i++ ) {
			toReturn.add(locationTimeArray[i], SpatialModel.buildSpatialModelFromAdjacencyMatrix(locations,edgeRecordHandler,graph[i]));
		}
		return toReturn;
	}

	static LocationService<Record> buildLocationServiceFromAdjacencyMatrix(int locations, RecordHandler edgeRecordHandler, double time,
																		   double[][][] graph) {
		LocationServiceList<Record> toReturn = new LocationServiceList<>();
		toReturn.add(time, SpatialModel.buildSpatialModelFromAdjacencyMatrix(locations,edgeRecordHandler,graph));
		return toReturn;
	}

	static LocationService<Record> buildLocationServiceFromAdjacencyList(int locations, RecordHandler edgeRecordHandler, double[] locationTimeArray,
																		 String[][][] graph) {
		LocationServiceList<Record> toReturn = new LocationServiceList<>();
		for( int i=0 ; i<locationTimeArray.length ; i++ ) {
			toReturn.add(locationTimeArray[i], SpatialModel.buildSpatialModelFromAdjacencyList(locations,edgeRecordHandler,graph[i]));
		}
		return toReturn;
	}

	static LocationService<Record> buildLocationServiceFromAdjacencyList(int locations, RecordHandler edgeRecordHandler, double time,
																		 String[][] graph) {
		LocationServiceList<Record> toReturn = new LocationServiceList<>();
		toReturn.add(time, SpatialModel.buildSpatialModelFromAdjacencyList(locations,edgeRecordHandler,graph));
		return toReturn;
	}

	static LocationService<Record> buildLocationServiceFromAdjacencyList(int locations, RecordHandler edgeRecordHandler, double[] locationTimeArray,
																		 double[][][] graph) {
		LocationServiceList<Record> toReturn = new LocationServiceList<>();
		for( int i=0 ; i<locationTimeArray.length ; i++ ) {
			toReturn.add(locationTimeArray[i], SpatialModel.buildSpatialModelFromAdjacencyList(locations,edgeRecordHandler,graph[i]));
		}
		return toReturn;
	}

	static LocationService<Record> buildLocationServiceFromAdjacencyList(int locations, RecordHandler edgeRecordHandler, double time,
																		 double[][] graph) {
		LocationServiceList<Record> toReturn = new LocationServiceList<>();
		toReturn.add(time, SpatialModel.buildSpatialModelFromAdjacencyList(locations,edgeRecordHandler,graph));
		return toReturn;
	}
}
