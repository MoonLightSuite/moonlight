package grid;

import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.LocationServiceList;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.util.TestUtils;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static eu.quanticol.moonlight.util.TestUtils.createSpatioTemporalSignal;

public class Experiment {

    private static final DecimalFormat df = new DecimalFormat("0.###");

    private final Supplier<SpatialTemporalMonitor> spatialTemporalMonitorSupplier;
    private final String formulaName;
    private final List<Integer> sizeGrid;
    private final List<Integer> tLength;

    public Experiment(Supplier<SpatialTemporalMonitor> spatialTemporalMonitorSupplier, String formulaName, List<Integer> sizeGrid, List<Integer> tLength) {
        this.spatialTemporalMonitorSupplier = spatialTemporalMonitorSupplier;
        this.formulaName = formulaName;
        this.sizeGrid = sizeGrid;
        this.tLength = tLength;
    }

    public void run(int n) {
        System.out.println();
        System.out.println("formula: " + formulaName);
        System.out.println("sizeGrid,tLength,mean,std");
        for (Integer s : sizeGrid) {
            for (Integer t : tLength) {
                //long start = System.currentTimeMillis();
                execute(n, spatialTemporalMonitorSupplier, s, t);
                //System.out.println("> "+(System.currentTimeMillis()-start)/(n*1000.0));
            }
        }
    }

    private void execute(int n, Supplier<SpatialTemporalMonitor> function, int sizeGrid, int tLength) {
        SpatialModel<Double> grid = TestUtils.createGridModel(sizeGrid, sizeGrid, false, 1.0);
        double[] times = IntStream.range(0, n).sequential().mapToDouble(i -> execTime(function.get(), grid, sizeGrid, tLength)).toArray();
        double mean = Arrays.stream(times).summaryStatistics().getAverage();
        double variance = Arrays.stream(times).map(time -> (time - mean) * (time - mean)).sum() / (n - 1);
        System.out.println(sizeGrid + "," + tLength + "," + df.format(mean) + "," + df.format(Math.sqrt(variance)));
    }

    private static float execTime(SpatialTemporalMonitor monitor, SpatialModel<Double> grid, int sizeGrid, int tLength) {
        Random rand = new Random(1);
        SpatialTemporalSignal<Double> signal = createSpatioTemporalSignal(sizeGrid * sizeGrid, 0, 1, tLength, (t, l) -> rand.nextDouble());
        LocationService<Double> locService = TestUtils.createLocServiceStatic(0, 1, tLength, grid);
        LocationServiceList<Double> staticLocService = new LocationServiceList<Double>();
        staticLocService.add(0,grid);
        long startingTime = System.currentTimeMillis();
        monitor.monitor(locService, signal);
        long endingTime = System.currentTimeMillis();
        float duration = (float) ((endingTime - startingTime) / 1000.0);
        return duration;
    }

}