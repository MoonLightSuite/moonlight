package eu.quanticol.moonlight.space;

import java.util.ArrayList;
import java.util.List;
import eu.quanticol.moonlight.util.Pair;

import java.util.Iterator;

public class StaticLocationService<V> implements LocationService<Double, V> {

    private SpatialModel<V> model;

    public StaticLocationService(SpatialModel<V> model) {
        this.model = model;
    }

    @Override
    public SpatialModel<V> get(Double t) {
        return model;
    }

    @Override
    public Iterator<Pair<Double, SpatialModel<V>>> times() {
        List<Pair<Double, SpatialModel<V>>> list = new ArrayList<>();
        list.add(new Pair<>(0.0,model));
        return list.iterator();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
