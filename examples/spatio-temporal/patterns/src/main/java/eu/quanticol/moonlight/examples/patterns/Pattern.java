package eu.quanticol.moonlight.examples.patterns;

import com.mathworks.engine.MatlabEngine;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.SpatialTemporalMonitoring;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.domain.BooleanDomain;
import eu.quanticol.moonlight.domain.DoubleDistance;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.core.space.DefaultDistanceStructure;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.Utils;
import eu.quanticol.moonlight.api.MatlabExecutor;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Pattern {

    public static void main(String[] args) throws InterruptedException, ExecutionException, URISyntaxException {
        String path = Paths.get(Pattern.class.getResource("TuringDataGenerator.m").toURI()).getParent().toAbsolutePath().toString();

        // %%%%%%%%%%  GRAPH  %%%%%%%%% //

        // Designing the grid
        SpatialModel<Double> gridModel = Utils.createGridModel(32, 32, false, 1.0);


        // %%%%%%%%%%%%% Connection with Matlab %%%%%%%%%%%%/////////
        MatlabEngine eng = MatlabExecutor.startMatlab();
        eng.eval("addpath(\""+ path+"\")");

        /// Generation of the trace
        eng.eval("TuringDataGenerator");
        double[][][] Atraj = eng.getVariable("Atraj");
        //double[][][] Btraj = eng.getVariable("Btraj");



        BiFunction<Double,Pair<Integer,Integer>, Double> gridFunction =  (t, pair) -> Atraj[(int)Math.round(t)][pair.getFirst()][pair.getSecond()];
        SpatialTemporalSignal<Double> signal = Utils.createSpatioTemporalSignalFromGrid(Atraj[0].length, Atraj[0][0].length, 0, 1, Atraj.length-1.0, gridFunction);
        LocationService<Double, Double> locService = Utils.createLocServiceStatic(0, 1, Atraj.length-1.0,gridModel);

    /*    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("/Users/lauretta/Desktop/aTraj.storage")))) {
            oos.writeObject(Atraj);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/




        // %%%%%%%%% PROPERTY %%%%%%% //
        double h_CONST_ = 0.5;
        double d1_CONST_ = 1;
        double d2_CONST_ = 6;
        double Tp_CONST_ = 38;
        double delta_CONST_ = 2;
        double Tend_CONST_ = 50;
        double dmax_CONST_ = 64;
        double dspot_CONST_ = 10;
        double hpert_CONST_ = 10;
        HashMap<String, Function<Parameters, Function<Double, Double>>> atomicFormulas = new HashMap<>();
        atomicFormulas.put("LowValues", p -> (x -> h_CONST_ - x));
        atomicFormulas.put("HighValues", p -> (x -> x - h_CONST_));


        HashMap<String, Function<Parameters, Function<Double, Boolean>>> atomicFormulasB = new HashMap<>();
        atomicFormulasB.put("LowValues", p -> (x -> x <= h_CONST_));
        atomicFormulasB.put("HighValues", p -> (x -> h_CONST_ >= x));

        HashMap<String, Function<SpatialModel<Double>, DefaultDistanceStructure<Double, ?>>> distanceFunctions = new HashMap<>();
        DefaultDistanceStructure<Double, Double> readD = new DefaultDistanceStructure<>(x -> x , new DoubleDistance(), 0.0, 6.0, gridModel);
        DefaultDistanceStructure<Double, Double> escapeD = new DefaultDistanceStructure<>(x -> x , new DoubleDistance(), 6.0, 32.0*32.0, gridModel);

        distanceFunctions.put("distReach", x -> readD);
        distanceFunctions.put("distEscape", x -> escapeD);

        Formula lValue = new AtomicFormula("LowValues");
        Formula hValue = new AtomicFormula("HighValues");

        Formula or12 = new OrFormula(lValue,hValue);
        Formula notOr12 = new NegationFormula(or12);

        Formula reachF = new ReachFormula(
                new AtomicFormula("LowValues"), "distReach", notOr12);
        Formula negReach = new NegationFormula(reachF);

        Formula escapeLow = new EscapeFormula("distEscape", new AtomicFormula("LowValues"));


        Formula negEsc = new NegationFormula(escapeLow);

        Formula surr = new AndFormula(new AndFormula(new AtomicFormula("LowValues"), negReach), negEsc);

        //// MONITOR /////
        SpatialTemporalMonitoring<Double, Double, Double> monitor =
                new SpatialTemporalMonitoring<>(
                        atomicFormulas,
                        distanceFunctions,
                        new DoubleDomain(),
                        true);

        SpatialTemporalMonitoring<Double, Double, Boolean> monitorB =
                new SpatialTemporalMonitoring<>(
                        atomicFormulasB,
                        distanceFunctions,
                        new BooleanDomain(),
                        true);


        SpatialTemporalMonitor<Double, Double, Double> m =
                monitor.monitor(surr, null);
        long start = System.currentTimeMillis();
        SpatialTemporalSignal<Double> sout = m.monitor(locService, signal);
        float elapsedTime = (System.nanoTime() - start)/1000F;
        List<Signal<Double>> signals = sout.getSignals();

        System.out.println(elapsedTime);
        System.out.println(signals.get(0).valueAt(0));
    }



}