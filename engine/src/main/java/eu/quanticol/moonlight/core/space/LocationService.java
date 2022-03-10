/*
 * MoonLight: a light-weight framework for runtime monitoring
 * Copyright (C) 2018-2021
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.quanticol.moonlight.core.space;

import java.util.Iterator;

import eu.quanticol.moonlight.core.base.MoonLightRecord;
import eu.quanticol.moonlight.offline.signal.RecordHandler;
import eu.quanticol.moonlight.space.LocationServiceList;
import eu.quanticol.moonlight.util.Pair;

/**
 * Primary interface of location services.
 * In general, a location service is anything that provides a spatial model
 * for a given time frame.
 *
 * @param <T> Time domain on which the location service is defined.
 * @param <E> Type on which the edges of the graph have been defined.
 *
 * @author loreti
 */
public interface LocationService<T, E> {

	/**
	 * Provides the spatial model corresponding to time t
	 * @param t time to be analyzed for the spatial model
	 * @return the spatial model that is active at time t.
	 */
	SpatialModel<E> get(T t);

	/**
	 * Provides an iterator over the pairs over which the location service is
	 * defined.
	 * TODO: better to use a chainIterator
	 * @return iterator over pairs of the location service
	 */
	Iterator<Pair<T, SpatialModel<E>>> times();

	/**
	 * Quick helper to check whether the location service is meaningful or not
	 * @return <code>true</code> when it can provide no spatial model,
     * 		   <code>false</code> otherwise
	 */
	boolean isEmpty();

	static LocationService<Double, MoonLightRecord> buildLocationServiceFromAdjacencyMatrix(int locations, RecordHandler edgeRecordHandler, double[] locationTimeArray,
                                                                                            String[][][][] graph) {
		LocationServiceList<MoonLightRecord> toReturn = new LocationServiceList<>();
		for( int i=0 ; i<locationTimeArray.length ; i++ ) {
			toReturn.add(locationTimeArray[i], SpatialModel.buildSpatialModelFromAdjacencyMatrix(locations,edgeRecordHandler,graph[i]));
		}
		return toReturn;
	}

	static LocationService<Double, MoonLightRecord> buildLocationServiceFromAdjacencyMatrix(int locations, RecordHandler edgeRecordHandler, double time,
                                                                                    String[][][] graph) {
		LocationServiceList<MoonLightRecord> toReturn = new LocationServiceList<>();
		toReturn.add(time, SpatialModel.buildSpatialModelFromAdjacencyMatrix(locations,edgeRecordHandler,graph));
		return toReturn;
	}

	static LocationService<Double, MoonLightRecord> buildLocationServiceFromAdjacencyMatrix(int locations, RecordHandler edgeRecordHandler, double[] locationTimeArray,
                                                                                    double[][][][] graph) {
		LocationServiceList<MoonLightRecord> toReturn = new LocationServiceList<>();
		for( int i=0 ; i<locationTimeArray.length ; i++ ) {
			toReturn.add(locationTimeArray[i], SpatialModel.buildSpatialModelFromAdjacencyMatrix(locations,edgeRecordHandler,graph[i]));
		}
		return toReturn;
	}

	static LocationService<Double, MoonLightRecord> buildLocationServiceFromAdjacencyMatrix(int locations, RecordHandler edgeRecordHandler, double time,
                                                                                    double[][][] graph) {
		LocationServiceList<MoonLightRecord> toReturn = new LocationServiceList<>();
		toReturn.add(time, SpatialModel.buildSpatialModelFromAdjacencyMatrix(locations,edgeRecordHandler,graph));
		return toReturn;
	}

	static LocationService<Double, MoonLightRecord> buildLocationServiceFromAdjacencyList(int locations, RecordHandler edgeRecordHandler, double[] locationTimeArray,
                                                                                  String[][][] graph) {
		LocationServiceList<MoonLightRecord> toReturn = new LocationServiceList<>();
		for( int i=0 ; i<locationTimeArray.length ; i++ ) {
			toReturn.add(locationTimeArray[i], SpatialModel.buildSpatialModelFromAdjacencyList(locations,edgeRecordHandler,graph[i]));
		}
		return toReturn;
	}

	static LocationService<Double, MoonLightRecord> buildLocationServiceFromAdjacencyList(int locations, RecordHandler edgeRecordHandler, double time,
                                                                                  String[][] graph) {
		LocationServiceList<MoonLightRecord> toReturn = new LocationServiceList<>();
		toReturn.add(time, SpatialModel.buildSpatialModelFromAdjacencyList(locations,edgeRecordHandler,graph));
		return toReturn;
	}

	static LocationService<Double, MoonLightRecord> buildLocationServiceFromAdjacencyList(int locations, RecordHandler edgeRecordHandler, double[] locationTimeArray,
                                                                                  double[][][] graph) {
		LocationServiceList<MoonLightRecord> toReturn = new LocationServiceList<>();
		for( int i=0 ; i<locationTimeArray.length ; i++ ) {
			toReturn.add(locationTimeArray[i], SpatialModel.buildSpatialModelFromAdjacencyList(locations,edgeRecordHandler,graph[i]));
		}
		return toReturn;
	}

	static LocationService<Double, MoonLightRecord> buildLocationServiceFromAdjacencyList(int locations, RecordHandler edgeRecordHandler, double time,
                                                                                  double[][] graph) {
		LocationServiceList<MoonLightRecord> toReturn = new LocationServiceList<>();
		toReturn.add(time, SpatialModel.buildSpatialModelFromAdjacencyList(locations,edgeRecordHandler,graph));
		return toReturn;
	}
}
