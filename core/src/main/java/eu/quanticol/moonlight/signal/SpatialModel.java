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

}