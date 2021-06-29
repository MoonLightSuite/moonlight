package grid;

import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.util.TestUtils;
import eu.quanticol.moonlight.util.Triple;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;

import static eu.quanticol.moonlight.util.TestUtils.createSpatioTemporalSignal;

public class Experiment {

    private final Function<SpatialModel<Double>, SpatialTemporalMonitor> function;
    private final String formulaName;
    private final Triple<Integer, Integer, Integer> sizeGrid;
    private final Triple<Integer, Integer, Integer> tLength;

    public Experiment(Function<SpatialModel<Double>, SpatialTemporalMonitor> function, String formulaName, Triple<Integer, Integer, Integer> sizeGrid, Triple<Integer, Integer, Integer> tLength) {
        this.function = function;
        this.formulaName = formulaName;
        this.sizeGrid = sizeGrid;
        this.tLength = tLength;
    }

    public void run(int n) {
        System.out.println();
        System.out.println("formula: " + formulaName);
        System.out.println("sizeGrid,tLength,mean,std");
        for (int s = sizeGrid.getFirst(); s < sizeGrid.getSecond(); s += sizeGrid.getThird()) {
            for (int t = tLength.getFirst(); t < tLength.getSecond(); t += tLength.getThird()) {
                execute(n, function, s, t);
            }
        }
    }

    private void execute(int n, Function<SpatialModel<Double>, SpatialTemporalMonitor> function, int sizeGrid, int tLength) {
        SpatialModel<Double> grid = TestUtils.createGridModel(sizeGrid, sizeGrid, false, 1.0);
        double[] times = IntStream.range(0, n).mapToDouble(i -> execTime(function.apply(grid), grid, sizeGrid, tLength)).toArray();
        double mean = Arrays.stream(times).summaryStatistics().getAverage();
        double variance = Arrays.stream(times).map(time -> (time - mean) * (time - mean)).sum() / (n - 1);
        System.out.println(sizeGrid + "," + tLength + "," + mean + "," + Math.sqrt(variance));
    }

    private static float execTime(SpatialTemporalMonitor monitor, SpatialModel<Double> grid, int sizeGrid, int tLength) {
        Random rand = new Random();
        SpatialTemporalSignal<Double> signal = createSpatioTemporalSignal(sizeGrid * sizeGrid, 0, 1, tLength, (t, l) -> rand.nextDouble());
        LocationService<Double> locService = TestUtils.createLocServiceStatic(0, 1, tLength, grid);
        long startingTime = System.currentTimeMillis();
        SpatialTemporalSignal<Double> sout = monitor.monitor(locService, signal);
        long endingTime = System.currentTimeMillis();
        float duration = (float) ((endingTime - startingTime) / 1000.0);

//        System.out.print("signal_at0 : " + signal.valuesatT(0) + "\n");
//        System.out.print("result_at0 : " + sout.valuesatT(0) + "\n");


        return duration;
    }

}
