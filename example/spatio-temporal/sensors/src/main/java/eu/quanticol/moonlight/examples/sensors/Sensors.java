package eu.quanticol.moonlight.examples.sensors;

import com.mathworks.engine.MatlabEngine;
import eu.quanticol.moonlight.formula.AtomicFormula;
import eu.quanticol.moonlight.formula.BooleanDomain;
import eu.quanticol.moonlight.formula.Parameters;
import eu.quanticol.moonlight.monitoring.SpatioTemporalMonitoring;
import eu.quanticol.moonlight.signal.GraphModelUtility;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;
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
        SpatioTemporalSignal<Integer> spatioTemporalSignal = new SpatioTemporalSignal<>(nodesType.length);
        IntStream.range(0, trajectory.length).forEach(i -> spatioTemporalSignal.add(i, location -> nodesType[location].intValue()));

        HashMap<String, Function<Parameters, Function<Integer, Boolean>>> atomicFormulas = new HashMap<>();
        atomicFormulas.put("type1", p -> (x -> x == 1));
        atomicFormulas.put("type2", p -> (x -> x == 2));
        atomicFormulas.put("type3", p -> (x -> x == 3));

        SpatioTemporalMonitoring<Double, Integer, Boolean> monitor =
                new SpatioTemporalMonitoring<>(
                        atomicFormulas,
                        null,
                        new BooleanDomain(),
                        false);


        BiFunction<DoubleFunction<SpatialModel<Double>>, SpatioTemporalSignal<Integer>, SpatioTemporalSignal<Boolean>> m =
                monitor.monitor(new AtomicFormula("type1"), null);
        SpatioTemporalSignal<Boolean> sout = m.apply(tConsumer, spatioTemporalSignal);
        System.out.println();
    }
}