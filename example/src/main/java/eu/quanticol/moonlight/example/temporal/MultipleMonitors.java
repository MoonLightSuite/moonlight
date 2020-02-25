package eu.quanticol.moonlight.example.temporal;

import eu.quanticol.moonlight.MoonLightScript;
import eu.quanticol.moonlight.TemporalScriptComponent;
import eu.quanticol.moonlight.xtext.ScriptLoader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

public class MultipleMonitors {
    public static void main(String[] args) throws IOException, URISyntaxException {
        fromFileScript();
        fromStringScript();
        fromJava();

    }

    private static void fromJava() {
    }

    private static void fromFileScript() throws URISyntaxException, IOException {
        // Load File Script
        URL multipleMonitorsUri = MultipleMonitors.class.getResource("multipleMonitors.mls");
        String multipleMonitorsPath = Paths.get(multipleMonitorsUri.toURI()).toString();
        ScriptLoader scriptLoader = new ScriptLoader();
        MoonLightScript moonLightScript = scriptLoader.loadFile(multipleMonitorsPath);
        TemporalScriptComponent<?> quantitativeMonitorScript = moonLightScript.selectTemporalComponent("QuantitativeMonitorScript");

        // Get signal
        double[] times = IntStream.range(0, 100).mapToDouble(s -> s).toArray();
        double[][] signals = toSignal(times, x -> x, x -> 3 * x);
        Object[][] monitorValues = quantitativeMonitorScript.monitorToDoubleArray(times, signals);

        // Print results
        printResults(monitorValues);
    }

    private static void fromStringScript() throws IOException {
        // Write a monitor script
        //@formatter:off
        String script = "monitor BooleanMonitorScript {\n" +
                "signal { real x; real y;}\n" +
                "domain boolean;\n" +
                "formula globally [0, 0.2]  #[ x > y ]#;\n" +
                "}\n" +
                "monitor QuantitativeMonitorScript{\n" +
                "signal { real x; real y;}\n" +
                "domain minmax;\n" +
                "formula globally [0, 0.2] #[ x - y ]#;\n" +
                "}";
        //@formatter:on
        // Load script
        ScriptLoader scriptLoader = new ScriptLoader();
        MoonLightScript moonLightScript = scriptLoader.compileScript(script);
        TemporalScriptComponent<?> quantitativeMonitorScript = moonLightScript.selectTemporalComponent("QuantitativeMonitorScript");

        // Get signal
        double[] times = IntStream.range(0, 100).mapToDouble(s -> s).toArray();
        double[][] signals = toSignal(times, x -> x, x -> 3 * x);
        Object[][] monitorValues = quantitativeMonitorScript.monitorToDoubleArray(times, signals);

        // Print results
        printResults(monitorValues);
    }

    private static void printResults(Object[][] monitorValues) {
        for (int i = 0; i < monitorValues.length; i++) {
            for (int j = 0; j < monitorValues[i].length; j++) {
                System.out.print(monitorValues[i][j]);
                System.out.print(" ");
            }
            System.out.println("");
        }
    }

    private static double[][] toSignal(double[] time, UnaryOperator<Double>... function) {
        double[][] signal = new double[time.length][function.length];
        Function<Double, double[]> doubleFunction = evalFunctions(function);
        for (int i = 0; i < time.length; i++) {
            signal[i] = doubleFunction.apply(time[i]);

        }
        return signal;
    }

    private static Function<Double, double[]> evalFunctions(UnaryOperator<Double>... functions) {
        return t -> Arrays.stream(functions).mapToDouble(s -> s.apply(t)).toArray();
    }
}