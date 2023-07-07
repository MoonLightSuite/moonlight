package grid;

import io.github.moonlightsuite.moonlight.core.formula.Formula;
import io.github.moonlightsuite.moonlight.core.space.DefaultDistanceStructure;
import io.github.moonlightsuite.moonlight.core.space.DistanceStructure;
import io.github.moonlightsuite.moonlight.core.space.LocationService;
import io.github.moonlightsuite.moonlight.core.space.SpatialModel;
import io.github.moonlightsuite.moonlight.domain.DoubleDomain;
import io.github.moonlightsuite.moonlight.formula.AtomicFormula;
import io.github.moonlightsuite.moonlight.formula.Parameters;
import io.github.moonlightsuite.moonlight.formula.spatial.SomewhereFormula;
import io.github.moonlightsuite.moonlight.offline.monitoring.SpatialTemporalMonitoring;
import io.github.moonlightsuite.moonlight.offline.monitoring.spatialtemporal.SpatialTemporalMonitor;
import io.github.moonlightsuite.moonlight.offline.signal.SpatialTemporalSignal;
import io.github.moonlightsuite.moonlight.util.Utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SimpleGrid {
    public static void main(String[] args) throws IOException, URISyntaxException {
        int sizeGrid = 10;
        int tLength = 1;
        float et = execTime(sizeGrid, tLength);
        System.out.print("executionTime : " + et);

    }

    private static float execTime(int sizeGrid, int tLength) {
        Random rand = new Random();
        SpatialModel<Double> grid = Utils.createGridModelAsGraph(sizeGrid, sizeGrid, false, 1.0);
        SpatialTemporalSignal<Double> signal = createSpatioTemporalSignal(sizeGrid * sizeGrid, 0, 1, tLength, (t, l) -> rand.nextDouble());
        LocationService<Double, Double> locService = Utils.createLocServiceStatic(0, 1, tLength, grid);

        HashMap<String, Function<SpatialModel<Double>, DistanceStructure<Double, ?>>> distanceFunctions = new HashMap<>();
        DefaultDistanceStructure<Double, Double> predist = new DefaultDistanceStructure<>(x -> x, new DoubleDomain(), 0.0, 5., grid);
        distanceFunctions.put("dist", x -> predist);
        HashMap<String, Function<Parameters, Function<Double, Double>>> atomic = new HashMap<>();
        atomic.put("simpleAtomic", p -> (x -> (x - 0.2)));
        Formula somewhere = new SomewhereFormula("dist", new AtomicFormula("simpleAtomic"));

        SpatialTemporalMonitoring<Double, Double, Double> monitor = new SpatialTemporalMonitoring<>(
                atomic,
                distanceFunctions,
                new DoubleDomain());

        SpatialTemporalMonitor<Double, Double, Double> m = monitor.monitor(
                somewhere);

        long startingTime = System.currentTimeMillis();
        SpatialTemporalSignal<Double> sout = m.monitor(locService, signal);
        long endingTime = System.currentTimeMillis();
        float duration = (float) ((endingTime - startingTime) / 1000.0);

        System.out.print("signal_at0 : " + signal.valuesAtT(0) + "\n");
        System.out.print("result_at0 : " + sout.valuesAtT(0) + "\n");


        return duration;
    }

    private static <T> SpatialTemporalSignal<T> createSpatioTemporalSignal(int size, double start, double dt, double end, BiFunction<Double, Integer, T> f) {
        SpatialTemporalSignal<T> s = new SpatialTemporalSignal(size);

        for (double time = start; time < end; time += dt) {
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
