package grid;

import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.SpatialTemporalMonitoring;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.util.Triple;

import java.util.HashMap;
import java.util.function.Function;


public class RunExperiment {

    public static void main(String[] args) {

        //first experiment
        Triple<Integer, Integer, Integer> sizeGrid = new Triple<>(10, 30, 10);
        Triple<Integer, Integer, Integer> tLength = new Triple<>(10, 30, 10);
        Experiment experiment = new Experiment(RunExperiment::getMonitorSomewhere, "somewhere(x>=0.2)", sizeGrid, tLength);
        experiment.run(2);

        //second experiment
        sizeGrid = new Triple<>(10, 30, 10);
        tLength = new Triple<>(10, 30, 10);
        experiment = new Experiment(RunExperiment::getMonitorReach, "(x>=0.2)reach(x<0.2)", sizeGrid, tLength);
        experiment.run(2);

    }

    private static SpatialTemporalMonitor getMonitorSomewhere(SpatialModel<Double> grid) {
        HashMap<String, Function<SpatialModel<Double>, DistanceStructure<Double, ?>>> distanceFunctions = new HashMap<>();
        DistanceStructure<Double, Double> predist = new DistanceStructure<>(x -> x, new DoubleDistance(), 0.0, 5., grid);
        distanceFunctions.put("dist", x -> predist);
        HashMap<String, Function<Parameters, Function<Double, Double>>> atomic = new HashMap<>();
        atomic.put("simpleAtomic", p -> (x -> (x - 0.2)));
        SpatialTemporalMonitoring<Double, Double, Double> monitor = new SpatialTemporalMonitoring<>(
                atomic,
                distanceFunctions,
                new DoubleDomain(),
                true);

        Formula somewhere = new SomewhereFormula("dist", new AtomicFormula("simpleAtomic"));
        return monitor.monitor(
                somewhere, null);
    }

    private static SpatialTemporalMonitor getMonitorReach(SpatialModel<Double> grid) {
        HashMap<String, Function<SpatialModel<Double>, DistanceStructure<Double, ?>>> distanceFunctions = new HashMap<>();
        DistanceStructure<Double, Double> predist = new DistanceStructure<>(x -> x, new DoubleDistance(), 0.0, 5., grid);
        distanceFunctions.put("dist", x -> predist);
        HashMap<String, Function<Parameters, Function<Double, Double>>> atomic = new HashMap<>();
        atomic.put("simpleAtomicRight", p -> (x -> (-x + 0.2)));
        atomic.put("simpleAtomicLeft", p -> (x -> (x - 0.2)));
        SpatialTemporalMonitoring<Double, Double, Double> monitor = new SpatialTemporalMonitoring<>(
                atomic,
                distanceFunctions,
                new DoubleDomain(),
                true);

        Formula somewhere = new ReachFormula(new AtomicFormula("simpleAtomicLeft"), "dist", new AtomicFormula("simpleAtomicRight"));
        return monitor.monitor(
                somewhere, null);
    }
}
