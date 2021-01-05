package eu.quanticol.moonlight.examples.sensors;

import eu.quanticol.moonlight.formula.BooleanDomain;
import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.TestUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

public class Sensor2 {

    private static SpatialModel<Double> sensor = buildingSensorNetwork();
    private static final double range = 40;
    private static final int SIZE = 7;
    private static final DoubleDomain doubleDomain = new DoubleDomain();
    private static final BooleanDomain booleanDomain = new BooleanDomain();

    public static void main(String[] argv) {
        List<Integer> typeOfNode = Arrays.asList(1, 3, 3, 3, 3);

        SpatialTemporalSignal<Integer> signal = createSpatioTemporalSignal(SIZE, 0, 1, 1.0,
                (t, l) -> new Integer(typeOfNode.get(l)));

    }

    private static SpatialModel<Double> buildingSensorNetwork() {
        HashMap<Pair<Integer, Integer>, Double> map = new HashMap<>();
        map.put(new Pair<>(0, 1), 1.0);
        map.put(new Pair<>(0, 3), 1.0);
        map.put(new Pair<>(0, 4), 1.0);
        map.put(new Pair<>(1, 0), 1.0);
        map.put(new Pair<>(1, 2), 1.0);
        map.put(new Pair<>(1, 4), 1.0);
        map.put(new Pair<>(2, 1), 1.0);
        map.put(new Pair<>(2, 3), 1.0);
        map.put(new Pair<>(2, 4), 1.0);
        map.put(new Pair<>(3, 0), 1.0);
        map.put(new Pair<>(3, 2), 1.0);
        map.put(new Pair<>(3, 4), 1.0);
        map.put(new Pair<>(4, 0), 1.0);
        map.put(new Pair<>(4, 1), 1.0);
        map.put(new Pair<>(4, 2), 1.0);
        map.put(new Pair<>(4, 3), 1.0);
        return TestUtils.createSpatialModel(SIZE, map);
    }

    private static <T> SpatialTemporalSignal<T> createSpatioTemporalSignal(int size, double start, double dt, double end, BiFunction<Double, Integer, T> f) {
        SpatialTemporalSignal<T> s = new SpatialTemporalSignal(size);

        for(double time = start; time < end; time += dt) {
            double finalTime = time;
            s.add(time, (i) -> {
                return f.apply(finalTime, i);
            });
        }

        s.add(end, (i) -> {
            return f.apply(end, i);
        });
        return s;
    }
}
