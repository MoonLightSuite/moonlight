package grid;

import eu.quanticol.moonlight.formula.BooleanDomain;
import eu.quanticol.moonlight.formula.DoubleDistance;
import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.SpatialModel;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;


public class RunExperiment {

    public static void main(String[] args) {
        List<Integer> sizeGrid = Arrays.asList(10, 100, 1000, 10000);
        List<Integer> tLength = Arrays.asList(1, 10, 100, 1000);
        int n = 10;

        System.out.println("numberOfExperiment: " + n);
        System.out.println("=========================");
        System.out.println("Boolean Monitor");
        System.out.println("=========================");
        //P1
        Experiment experiment = new Experiment(RunExperiment::getMonitorReachBoolean, "(x<=0.5)reach[0,30](x>0.5)", sizeGrid, tLength);
        experiment.run(n);

        //P2
        experiment = new Experiment(RunExperiment::getMonitorEscapeBoolean, "escape[5,INF](x>=0.5)", sizeGrid, tLength);
        experiment.run(n);

        //P3
        experiment = new Experiment(RunExperiment::getMonitorSomewhereBoolean, "somewhere[0,30](x>=0.5)", sizeGrid, tLength);
        experiment.run(n);

        //SPT1
        experiment = new Experiment(RunExperiment::getMonitorSPTReachBoolean, "(x<=0.5)reach[0,30]eventually(x>0.5)", sizeGrid, tLength);
        experiment.run(n);

        //TSP1
        experiment = new Experiment(RunExperiment::getMonitorTSPReachBoolean, "globally{(x<=0.5)reach[0,30](x>0.5)}", sizeGrid, tLength);
        experiment.run(n);

        System.out.println("=========================");
        System.out.println("MinMax Monitor");
        System.out.println("=========================");
        //P1
        experiment = new Experiment(RunExperiment::getMonitorReachMinMax, "(x<=0.5)reach[0,30](x>0.5)", sizeGrid, tLength);
        experiment.run(n);

        //P2
        experiment = new Experiment(RunExperiment::getMonitorEscapeMinMax, "escape[5,INF](x>=0.5)", sizeGrid, tLength);
        experiment.run(n);

        //P3
        experiment = new Experiment(RunExperiment::getMonitorSomewhereMinMax, "somewhere[0,30](x>=0.5)", sizeGrid, tLength);
        experiment.run(n);

        //SPT1
        experiment = new Experiment(RunExperiment::getMonitorSPTReachMinMax, "(x<=0.5)reach[0,30]eventually(x>0.5)", sizeGrid, tLength);
        experiment.run(n);

        //TSP1
        experiment = new Experiment(RunExperiment::getMonitorTSPReachMinMax, "globally{(x<=0.5)reach[0,30](x>0.5)}", sizeGrid, tLength);
        experiment.run(n);


    }

    private static <T> SpatialTemporalMonitor<Double, Double, T> atomicSignal(Function<Double, T> predicate) {
        return SpatialTemporalMonitor.atomicMonitor(predicate);
    }

    private static Function<SpatialModel<Double>, DistanceStructure<Double, ?>> distance(double from, double to) {
        return g -> new DistanceStructure<>(x -> x, new DoubleDistance(), from, to, g);
    }

    private static SpatialTemporalMonitor getMonitorEscapeBoolean() {
        SpatialTemporalMonitor<Double, Double, Boolean> atomic = atomicSignal(x -> x > 0.5);
        return SpatialTemporalMonitor.escapeMonitor(atomic, distance(5, Double.MAX_VALUE), new BooleanDomain());
    }

    private static SpatialTemporalMonitor getMonitorEscapeMinMax() {
        SpatialTemporalMonitor<Double, Double, Double> atomic = atomicSignal(x -> x - 0.5);
        return SpatialTemporalMonitor.somewhereMonitor(atomic, distance(5, Double.MAX_VALUE), new DoubleDomain());
    }


    private static SpatialTemporalMonitor getMonitorSomewhereBoolean() {
        SpatialTemporalMonitor<Double, Double, Boolean> atomic = atomicSignal(x -> x > 0.5);
        return SpatialTemporalMonitor.somewhereMonitor(atomic, distance(0, 30), new BooleanDomain());
    }

    private static SpatialTemporalMonitor getMonitorSomewhereMinMax() {
        SpatialTemporalMonitor<Double, Double, Double> atomic = atomicSignal(x -> x - 0.5);
        return SpatialTemporalMonitor.somewhereMonitor(atomic, distance(0, 30), new DoubleDomain());
    }

    private static SpatialTemporalMonitor getMonitorReachBoolean() {
        SpatialTemporalMonitor<Double, Double, Boolean> left = atomicSignal(x -> x <= 0.5);
        SpatialTemporalMonitor<Double, Double, Boolean> right = atomicSignal(x -> x > 0.5);
        return SpatialTemporalMonitor.reachMonitor(left, distance(0, 30), right, new BooleanDomain());
    }

    private static SpatialTemporalMonitor getMonitorReachMinMax() {
        SpatialTemporalMonitor<Double, Double, Double> left = atomicSignal(x -> 0.5 - x);
        SpatialTemporalMonitor<Double, Double, Double> right = atomicSignal(x -> x - 0.5);
        return SpatialTemporalMonitor.reachMonitor(left, distance(0, 30), right, new DoubleDomain());
    }

    private static SpatialTemporalMonitor getMonitorSPTReachBoolean() {
        SpatialTemporalMonitor<Double, Double, Boolean> left = atomicSignal(x -> x <= 0.5);
        SpatialTemporalMonitor<Double, Double, Boolean> right = SpatialTemporalMonitor.eventuallyMonitor(atomicSignal(x -> x > 0.5), new BooleanDomain());
        return SpatialTemporalMonitor.reachMonitor(left, distance(0, 30), right, new BooleanDomain());
    }

    private static SpatialTemporalMonitor getMonitorSPTReachMinMax() {
        SpatialTemporalMonitor<Double, Double, Double> left = atomicSignal(x -> 0.5 - x);
        SpatialTemporalMonitor<Double, Double, Double> right = SpatialTemporalMonitor.eventuallyMonitor(atomicSignal(x -> x - 0.5), new DoubleDomain());
        return SpatialTemporalMonitor.reachMonitor(left, distance(0, 30), right, new DoubleDomain());
    }

    private static SpatialTemporalMonitor getMonitorTSPReachBoolean() {
        return SpatialTemporalMonitor.globallyMonitor(getMonitorReachBoolean(), new BooleanDomain());
    }

    private static SpatialTemporalMonitor getMonitorTSPReachMinMax() {
        return SpatialTemporalMonitor.globallyMonitor(getMonitorReachMinMax(), new DoubleDomain());
    }
}