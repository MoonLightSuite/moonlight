package eu.quanticol.moonlight.examples.sensors;

import com.mathworks.engine.MatlabEngine;
import eu.quanticol.moonlight.signal.GraphModelUtility;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.utility.matlab.MatlabExecutor;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.function.DoubleFunction;

public class Sensors {

    public static void main(String[] args) throws URISyntaxException, ExecutionException, InterruptedException {
        String path = Paths.get(Sensors.class.getResource("mobility/mobility.m").toURI()).getParent().toAbsolutePath().toString();
        MatlabEngine eng = MatlabExecutor.startMatlab();
        eng.eval("addpath(\"" + path + "\")");

        /// Generation of the trace
        eng.eval("mobility");

        Object[][] trajectory = eng.getVariable("nodes");
        Object[] nodesType = eng.getVariable("nodes_type");
        Object[] cgraph1 = eng.getVariable("cgraph1");
        Object[] cgraph2 = eng.getVariable("cgraph2");
        MatlabExecutor.close();
        DoubleFunction<SpatialModel<Double>> tConsumer = t -> GraphModelUtility.fromMatrix((double[][]) cgraph1[(int) Math.floor(t)]);
        System.out.println();

    }
}