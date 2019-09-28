package eu.quanticol.moonlight.tests;

import eu.quanticol.moonlight.signal.GraphModel;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;
import eu.quanticol.moonlight.util.Pair;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author loreti
 */
class TestUtils {

    private TestUtils() {
        //utility class
    }

    static <T> Signal<T> createSignal(double start, double end, double dt, Function<Double, T> f) {
        Signal<T> signal = new Signal<>();
        double time = start;
        while (time <= end) {
            signal.add(time, f.apply(time));
            time += dt;
        }
        signal.endAt(end);
        return signal;
    }


    static <T> SpatioTemporalSignal<T> createSpatioTemporalSignal(int size, double start, double dt, double end, BiFunction<Double, Integer, T> f) {
        SpatioTemporalSignal<T> s = new SpatioTemporalSignal<>(size);
        double time = start;
        while (time < end) {
            double current = time;
            s.add(time, (i -> f.apply(current, i)));
            time += dt;
        }
        return s;
    }

    static <T> SpatialModel<T> createSpatialModel(int size, Map<Pair<Integer, Integer>, T> edges) {
        return createSpatialModel(size, (i, j) -> edges.get(new Pair<>(i, j)));
    }

    static <T> SpatialModel<T> createSpatialModel(int size, BiFunction<Integer, Integer, T> edges) {
        GraphModel<T> model = new GraphModel<>(size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                T value = edges.apply(i, j);
                if (value != null) {
                    model.add(i, value, j);
                }
            }
        }
        return model;
    }

    static <T> SpatialModel<T> createGridModel(int rows, int columns, boolean directed, T w) {
        int size = rows * columns;
        GraphModel<T> model = new GraphModel<>(size);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (i + 1 < rows) {
                    model.add(gridIndexOf(i, j, columns), w, gridIndexOf(i + 1, j, columns));
                    if (!directed) {
                        model.add(gridIndexOf(i + 1, j, columns), w, gridIndexOf(i, j, columns));
                    }
                }
                if (j + 1 < columns) {
                    model.add(gridIndexOf(i, j, columns), w, gridIndexOf(i, j + 1, columns));
                    if (!directed) {
                        model.add(gridIndexOf(i, j + 1, columns), w, gridIndexOf(i, j, columns));
                    }
                }
            }
        }
        return model;
    }


    static int gridIndexOf(int r, int c, int columns) {
        return r * columns + c;
    }


    static Pair<Integer, Integer> gridLocationOf(int i, int rows, int columns) {
        int r = i / columns;
        int c = i % columns;
        if ((r >= rows) || (c >= columns)) {
            throw new IllegalArgumentException();
        }
        return new Pair<>(r, c);
    }

}