package io.github.moonlightsuite.moonlight.examples.sensors;

import com.mathworks.engine.MatlabEngine;
import io.github.moonlightsuite.moonlight.api.MatlabExecutor;
import io.github.moonlightsuite.moonlight.api.configurator.MatlabDataConverter;
import io.github.moonlightsuite.moonlight.core.base.Pair;
import io.github.moonlightsuite.moonlight.core.formula.Formula;
import io.github.moonlightsuite.moonlight.core.space.DefaultDistanceStructure;
import io.github.moonlightsuite.moonlight.core.space.DistanceStructure;
import io.github.moonlightsuite.moonlight.core.space.LocationService;
import io.github.moonlightsuite.moonlight.core.space.SpatialModel;
import io.github.moonlightsuite.moonlight.domain.BooleanDomain;
import io.github.moonlightsuite.moonlight.domain.DoubleDomain;
import io.github.moonlightsuite.moonlight.formula.AtomicFormula;
import io.github.moonlightsuite.moonlight.formula.Parameters;
import io.github.moonlightsuite.moonlight.formula.spatial.SomewhereFormula;
import io.github.moonlightsuite.moonlight.offline.monitoring.SpatialTemporalMonitoring;
import io.github.moonlightsuite.moonlight.offline.monitoring.spatialtemporal.SpatialTemporalMonitor;
import io.github.moonlightsuite.moonlight.offline.signal.Signal;
import io.github.moonlightsuite.moonlight.offline.signal.SpatialTemporalSignal;
import io.github.moonlightsuite.moonlight.util.Utils;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Sensors {

    public static void main(String[] args) throws URISyntaxException, ExecutionException, InterruptedException {
        String path = Paths.get(Sensors.class.getResource("mobility.m").toURI()).getParent().toAbsolutePath().toString();
        MatlabEngine eng = MatlabExecutor.startMatlab();
        eng.eval("addpath(\"" + path + "\")");

        /// Generation of the trace
        eng.eval("mobility");

        // TODO: The 'nodes' variable apparently makes JVM crash
        //       with MatlabEngine2021
        //Object[][] trajectory = eng.getVariable("nodes");
        double nodes = eng.getVariable("num_nodes");
        Double[] nodesType = MatlabDataConverter.getArray(eng.getVariable("nodes_type"), Double.class);
        Object[] cgraph1 = eng.getVariable("cgraph1");
        Object[] cgraph2 = eng.getVariable("cgraph2");
        MatlabExecutor.close();
        LocationService<Double, Double> tConsumer = Utils.createLocServiceFromSetMatrix(cgraph1);
        SpatialTemporalSignal<Pair<Integer, Integer>> spatialTemporalSignal = new SpatialTemporalSignal<>(nodesType.length);
        IntStream.range(0, (int) nodes - 1).forEach(i -> spatialTemporalSignal.add(i, (location -> new Pair<>(nodesType[location].intValue(), i))));

        HashMap<String, Function<Parameters, Function<Pair<Integer, Integer>, Boolean>>> atomicFormulas = new HashMap<>();
        atomicFormulas.put("type1", p -> (x -> x.getFirst() == 1));
        atomicFormulas.put("type2", p -> (x -> x.getFirst() == 2));
        atomicFormulas.put("type3", p -> (x -> x.getFirst() == 3));

        HashMap<String, Function<SpatialModel<Double>, DistanceStructure<Double, ?>>> distanceFunctions = new HashMap<>();
        distanceFunctions.put("dist", m -> new DefaultDistanceStructure<>(x -> x, new DoubleDomain(), 0.0, 1.0, m));

        Formula isType1 = new AtomicFormula("type1");
        Formula somewhere = new SomewhereFormula("dist", isType1);

        SpatialTemporalMonitoring<Double, Pair<Integer, Integer>, Boolean> monitor =
                new SpatialTemporalMonitoring<>(
                        atomicFormulas,
                        distanceFunctions,
                        new BooleanDomain());


        SpatialTemporalMonitor<Double, Pair<Integer, Integer>, Boolean> m =
                monitor.monitor(somewhere);
        SpatialTemporalSignal<Boolean> sout = m.monitor(tConsumer, spatialTemporalSignal);
        List<Signal<Boolean>> signals = sout.getSignals();
        System.out.println(signals.get(0));
    }
}
