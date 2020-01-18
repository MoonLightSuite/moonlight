package eu.quanticol.moonlight.signal;

import eu.quanticol.moonlight.util.Pair;

import java.util.List;
import java.util.Set;

public interface SpatialModel<T> {

    T get(int src, int trg);

    int size();

    List<Pair<Integer, T>> next(int l);

    List<Pair<Integer, T>> previous(int l);

    Set<Integer> getLocations();

	static SpatialModel<Record> buildSpatialModel(int locations, RecordHandler edgeRecordHandler,
			Object[][][] objects) {
		GraphModel<Record> toReturn = new GraphModel<>(locations);
		//TODO: Fill code here!		
		return toReturn;
	}

}