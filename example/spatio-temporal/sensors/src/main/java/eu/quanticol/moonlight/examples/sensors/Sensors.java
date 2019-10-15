package eu.quanticol.moonlight.examples.sensors;

import com.mathworks.engine.MatlabEngine;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.SpatioTemporalMonitoring;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.GraphModelUtility;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.utility.matlab.MatlabExecutor;
import eu.quanticol.moonlight.utility.matlab.configurator.MatlabDataConverter;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.DoubleFunction;
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
        DoubleFunction<SpatialModel<Double>> tConsumer = t -> GraphModelUtility.fromMatrix((double[][]) cgraph1[(int) Math.floor(t)]);
        SpatioTemporalSignal<Pair<Integer, Integer>> spatioTemporalSignal = new SpatioTemporalSignal<>(nodesType.length);
        IntStream.range(0, trajectory.length-1).forEach(i -> spatioTemporalSignal.add(i, (location -> new Pair<>(nodesType[location].intValue(),i))));

        HashMap<String, Function<Parameters, Function<Pair<Integer,Integer>, Boolean>>> atomicFormulas = new HashMap<>();
        atomicFormulas.put("type1", p -> (x -> x.getFirst() == 1));
        atomicFormulas.put("type2", p -> (x -> x.getFirst() == 2));
        atomicFormulas.put("type3", p -> (x -> x.getFirst() == 3));

        HashMap<String, Function<SpatialModel<Double>, DistanceStructure<Double, ?>>> distanceFunctions = new HashMap<>();
        distanceFunctions.put("dist", m -> new DistanceStructure<>(x -> x , new DoubleDistance(), 0.0, 1.0, m));

        Formula isType1 =new AtomicFormula("type1");
        Formula somewhere = new SomewhereFormula("dist",isType1);

        SpatioTemporalMonitoring<Double, Pair<Integer, Integer>, Boolean> monitor =
                new SpatioTemporalMonitoring<>(
                        atomicFormulas,
                        distanceFunctions,
                        new BooleanDomain(),
                        false);


        BiFunction<DoubleFunction<SpatialModel<Double>>, SpatioTemporalSignal<Pair<Integer, Integer>>, SpatioTemporalSignal<Boolean>> m =
                monitor.monitor(somewhere, null);
        SpatioTemporalSignal<Boolean> sout = m.apply(tConsumer, spatioTemporalSignal);
        System.out.println(sout.getSignals().toArray());
    }
}