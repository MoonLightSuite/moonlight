package eu.quanticol.moonlight.examples.simpleTemporal;

import eu.quanticol.moonlight.MoonLightScript;
import eu.quanticol.moonlight.TemporalScriptComponent;
import eu.quanticol.moonlight.domain.BooleanDomain;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.core.formula.Interval;
import eu.quanticol.moonlight.offline.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.script.MoonLightScriptLoaderException;
import eu.quanticol.moonlight.script.ScriptLoader;
import eu.quanticol.moonlight.core.io.DataHandler;
import eu.quanticol.moonlight.offline.signal.Signal;
import eu.quanticol.moonlight.core.base.Pair;
import eu.quanticol.moonlight.util.Utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

public class MultipleMonitors {
    public static void main(String[] args) throws IOException, URISyntaxException, MoonLightScriptLoaderException {
        //fromJava();
        fromFileScript();
        //fromStringScript();
    }

    private static void fromJava() {
        // Get signal
        Signal<Pair<Double,Double>> signal = Utils.createSignal(0.0, 50, 1.0, x -> new Pair<>( x, 3 * x));


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

    private static void fromFileScript() throws URISyntaxException, IOException, MoonLightScriptLoaderException {
        // Load File Script
        URL multipleMonitorsUri = MultipleMonitors.class.getResource("booleanmonitor.mls");
        String multipleMonitorsPath = Paths.get(multipleMonitorsUri.toURI()).toString();
        MoonLightScript moonLightScript = ScriptLoader.loaderFromFile(multipleMonitorsPath).getScript();
        TemporalScriptComponent<?> booleanMonitorScript = moonLightScript.temporal().selectDefaultTemporalComponent();


        // Get signal
        double[] times = IntStream.range(0, 51).mapToDouble(s -> s).toArray();
        double[][] signals = toSignal(times, x -> x, x -> 3 * x);

        // Monitoring
        double[][] monitorValuesB = booleanMonitorScript.monitorToArray(times, signals);


        // Print results
        System.out.print("fromFileScript Boolean\n");
        printResults(monitorValuesB);
    }

    private static void fromStringScript() throws IOException, MoonLightScriptLoaderException {
        // Write a monitor script
        //@formatter:off
        String script = "signal { real x; real y;}\n" +
                "domain minmax;\n" +
                "formula main = globally ( x - y );\n";
        //@formatter:on
        // Load script
        MoonLightScript moonLightScript = ScriptLoader.loaderFromCode(script).getScript();
        // Choose the monitor
        TemporalScriptComponent<?> quantitativeMonitorScript = moonLightScript.temporal().selectDefaultTemporalComponent();

        // Get signal
        double[] times = IntStream.range(0, 50).mapToDouble(s -> s).toArray();
        double[][] signals = toSignal(times, x -> x, x -> 3 * x);
        double[][] monitorValues = quantitativeMonitorScript.monitorToArray(times, signals);

        // Print results
        System.out.print("fromStringScript Quantitative \n");
        printResults(monitorValues);

        script = "signal { real x; real y;}\n" +
                "domain boolean;\n" +
                "formula globally ( x > y );\n";

        moonLightScript = ScriptLoader.loaderFromCode(script).getScript();
        // Choose the monitor

        // Choose the monitor
        TemporalScriptComponent<?> BooleanMonitorScript = moonLightScript.temporal().selectDefaultTemporalComponent();

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