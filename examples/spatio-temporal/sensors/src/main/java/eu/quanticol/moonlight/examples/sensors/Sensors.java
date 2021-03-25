package eu.quanticol.moonlight.examples.sensors;

import com.mathworks.engine.MatlabEngine;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.SpatialTemporalMonitoring;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.domain.BooleanDomain;
import eu.quanticol.moonlight.domain.DoubleDistance;
import eu.quanticol.moonlight.space.DistanceStructure;
import eu.quanticol.moonlight.space.LocationService;
import eu.quanticol.moonlight.space.SpatialModel;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.TestUtils;
import eu.quanticol.moonlight.utility.matlab.MatlabExecutor;
import eu.quanticol.moonlight.utility.matlab.configurator.MatlabDataConverter;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Sensors {

    public static void main(String[] args) throws URISyntaxException, ExecutionException, InterruptedException {
        String path = Paths.get(Sensors.class.getResource("mobility/mobility.m").toURI()).getParent().toAbsolutePath().toString();
        MatlabEngine eng = MatlabExecutor.startMatlab();
        eng.eval("addpath(\"" + path + "\")");

        /// Generation of the trace
        eng.eval("mobility");

        Object[][] trajectory = eng.getVariable("nodes");
        Double[] nodesType = MatlabDataConverter.getArray(eng.getVariable("nodes_type"), Double.class);
        Object[] cgraph1 = eng.getVariable("cgraph1");
        Object[] cgraph2 = eng.getVariable("cgraph2");
        MatlabExecutor.close();
        LocationService<Double, Double> tConsumer = TestUtils.createLocServiceFromSetMatrix(cgraph1);
        SpatialTemporalSignal<Pair<Integer, Integer>> spatialTemporalSignal = new SpatialTemporalSignal<>(nodesType.length);
        IntStream.range(0, trajectory.length-1).forEach(i -> spatialTemporalSignal.add(i, (location -> new Pair<>(nodesType[location].intValue(),i))));

        HashMap<String, Function<Parameters, Function<Pair<Integer,Integer>, Boolean>>> atomicFormulas = new HashMap<>();
        atomicFormulas.put("type1", p -> (x -> x.getFirst() == 1));
        atomicFormulas.put("type2", p -> (x -> x.getFirst() == 2));
        atomicFormulas.put("type3", p -> (x -> x.getFirst() == 3));

        HashMap<String, Function<SpatialModel<Double>, DistanceStructure<Double, ?>>> distanceFunctions = new HashMap<>();
        distanceFunctions.put("dist", m -> new DistanceStructure<>(x -> x , new DoubleDistance(), 0.0, 1.0, m));

        Formula isType1 =new AtomicFormula("type1");
        Formula somewhere = new SomewhereFormula("dist",isType1);

        SpatialTemporalMonitoring<Double, Pair<Integer, Integer>, Boolean> monitor =
                new SpatialTemporalMonitoring<>(
                        atomicFormulas,
                        distanceFunctions,
                        new BooleanDomain(),
                        false);


        SpatialTemporalMonitor<Double, Pair<Integer, Integer>, Boolean> m =
                monitor.monitor(somewhere, null);
        SpatialTemporalSignal<Boolean> sout = m.monitor(tConsumer, spatialTemporalSignal);
        List<Signal<Boolean>> signals = sout.getSignals();
        System.out.println(signals.get(0));
    }
}