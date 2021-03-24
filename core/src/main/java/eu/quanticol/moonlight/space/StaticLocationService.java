package eu.quanticol.moonlight.space;

import java.util.List;
import eu.quanticol.moonlight.util.Pair;

import java.util.Iterator;

public class StaticLocationService<V> implements LocationService<V> {

    private SpatialModel<V> model;

    public StaticLocationService(SpatialModel<V> model) {
        this.model = model;
    }

    @Override
    public SpatialModel<V> get(double t) {
        return model;
    }

    @Override
    public Iterator<Pair<Double, SpatialModel<V>>> times() {
        return List.of(new Pair<>(0.0,model)).iterator();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
