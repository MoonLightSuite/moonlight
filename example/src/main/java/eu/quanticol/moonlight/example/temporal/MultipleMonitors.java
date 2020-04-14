package eu.quanticol.moonlight.example.temporal;

import eu.quanticol.moonlight.MoonLightScript;
import eu.quanticol.moonlight.TemporalScriptComponent;
import eu.quanticol.moonlight.formula.BooleanDomain;
import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.DataHandler;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.TestUtils;
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
        //fromJava();
        //fromFileScript();
        fromStringScript();
    }

    private static void fromJava() {
        // Get signal
        Signal<Pair<Double,Double>> signal = TestUtils.createSignal(0.0, 50, 1.0, x -> new Pair<>( x, 3 * x));


        // Build the property (Boolean Semantics)
        TemporalMonitor<Pair<Double,Double>,Boolean> mB = TemporalMonitor.globallyMonitor(
                TemporalMonitor.atomicMonitor(x -> x.getFirst()>x.getSecond()), new BooleanDomain(),new Interval(0,0.2));

        // Monitoring
        Signal<Boolean> soutB = mB.monitor(signal);
        double[][] monitorValuesB = soutB.arrayOf(DataHandler.BOOLEAN::doubleOf);
        // Print results
        System.out.print("fromJava Boolean\n");
        printResults(monitorValuesB);

        // Build the property (Quantitative Semantics)
        TemporalMonitor<Pair<Double,Double>,Double> mQ = TemporalMonitor.globallyMonitor(
                TemporalMonitor.atomicMonitor(x -> x.getFirst()-x.getSecond()), new DoubleDomain(),new Interval(0,0.2));
        Signal<Double> soutQ = mQ.monitor(signal);
        double[][] monitorValuesQ = soutQ.arrayOf(DataHandler.REAL::doubleOf);
        // Print results
        System.out.print("fromJava Quantitative \n");
        printResults(monitorValuesQ);


    }

    private static void fromFileScript() throws URISyntaxException, IOException {
        // Load File Script
        URL multipleMonitorsUri = MultipleMonitors.class.getResource("multipleMonitors.mls");
        String multipleMonitorsPath = Paths.get(multipleMonitorsUri.toURI()).toString();
        ScriptLoader scriptLoader = new ScriptLoader();
        MoonLightScript moonLightScript = scriptLoader.loadFile(multipleMonitorsPath);
        TemporalScriptComponent<?> booleanMonitorScript = moonLightScript.selectTemporalComponent("BooleanMonitorScript");


        // Get signal
        double[] times = IntStream.range(0, 51).mapToDouble(s -> s).toArray();
        double[][] signals = toSignal(times, x -> x, x -> 3 * x);

        // Monitoring
        double[][] monitorValuesB = booleanMonitorScript.monitorToArray(times, signals);


        // Print results
        System.out.print("fromFileScript Boolean\n");
        printResults(monitorValuesB);
    }

    private static void fromStringScript() throws IOException {
        // Write a monitor script
        //@formatter:off
        String script = "monitor BooleanMonitorScript {\n" +
                "signal { real x; real y;}\n" +
                "domain boolean;\n" +
                "formula globally #[ x > y ]#;\n" +
                "}\n" +
                "monitor QuantitativeMonitorScript{\n" +
                "signal { real x; real y;}\n" +
                "domain minmax;\n" +
                "formula globally #[ x - y ]#;\n" +
                "}";
        //@formatter:on
        // Load script
        ScriptLoader scriptLoader = new ScriptLoader();
        MoonLightScript moonLightScript = scriptLoader.compileScript(script);
        // Choose the monitor
        TemporalScriptComponent<?> quantitativeMonitorScript = moonLightScript.selectTemporalComponent("QuantitativeMonitorScript");

        // Get signal
        double[] times = IntStream.range(0, 50).mapToDouble(s -> s).toArray();
        double[][] signals = toSignal(times, x -> x, x -> 3 * x);
        double[][] monitorValues = quantitativeMonitorScript.monitorToArray(times, signals);

        // Print results
        System.out.print("fromStringScript Quantitative \n");
        printResults(monitorValues);

        // Choose the monitor
        TemporalScriptComponent<?> BooleanMonitorScript = moonLightScript.selectTemporalComponent("BooleanMonitorScript");

        // Get signal
        double[] timesB = IntStream.range(0, 50).mapToDouble(s -> s).toArray();
        double[][] signalsB = toSignal(times, x -> x, x -> 3 * x);
        double[][] monitorValuesB = BooleanMonitorScript.monitorToArray(times, signals);

        // Print results
        System.out.print("fromStringScript Boolean \n");
        printResults(monitorValuesB);
    }

    private static void printResults(double[][] monitorValues) {
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