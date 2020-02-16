package eu.quanticol.moonlight.signal;

import eu.quanticol.moonlight.util.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public interface SpatialModel<T> {

    T get(int src, int trg);

    int size();

    List<Pair<Integer, T>> next(int l);

    List<Pair<Integer, T>> previous(int l);

    Set<Integer> getLocations();

    static SpatialModel<Record> buildSpatialModel(int locations, RecordHandler edgeRecordHandler,
                                                  String[][][] objects) {
        GraphModel<Record> toReturn = new GraphModel<>(locations);
        for (int i = 0; i < objects.length; i++) {
            for (int j = 0; j < objects[i].length; j++) {
                if (i != j && isFull(objects[i][j])) {
                    toReturn.add(i, edgeRecordHandler.fromString(objects[i][j]), j);
                }
            }
        }
        return toReturn;
    }

    static SpatialModel<Record> buildSpatialModel(int locations, RecordHandler edgeRecordHandler,
                                                  double[][][] objects) {
        GraphModel<Record> toReturn = new GraphModel<>(locations);
        for (int i = 0; i < objects.length; i++) {
            for (int j = 0; j < objects[i].length; j++) {
                if (i != j && isFull(objects[i][j])) {
                    toReturn.add(i, edgeRecordHandler.fromDouble(objects[i][j]), j);
                }
            }
        }
        return toReturn;
    }


    static Boolean isFull(String[] array) {
        return !Arrays.stream(array).allMatch(Objects::isNull);
    }

    static Boolean isFull(double[] array) {
        return !Arrays.stream(array).allMatch(Objects::isNull);
    }

}