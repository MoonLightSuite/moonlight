package eu.quanticol.moonlight.examples.patterns;

import com.mathworks.engine.MatlabEngine;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.SpatioTemporalMonitoring;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.TestUtils;
import eu.quanticol.moonlight.util.Triple;
import eu.quanticol.moonlight.utility.matlab.MatlabExecutor;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.DoubleFunction;
import java.util.function.Function;

public class Pattern {



    public static void main(String[] args) throws InterruptedException, ExecutionException, URISyntaxException {
        URI resource = new URI(Pattern.class.getResource("TuringDataGenerator.m").getPath()).resolve(".");
        String path = resource.getPath();

        // %%%%%%%%%%  GRAPH  %%%%%%%%% //

        // Designing the grid
        SpatialModel<Double> gridModel = TestUtils.createGridModel(32, 32, false, 1.0);


        // %%%%%%%%%%%%% Connection with Matlab %%%%%%%%%%%%/////////
        MatlabEngine eng = MatlabExecutor.startMatlab();
        eng.eval("addpath(\""+ path+"\")");

        /// Generation of the trace
        eng.eval("TuringDataGenerator");
        double[][][] Atraj = eng.getVariable("Atraj");
        //double[][][] Btraj = eng.getVariable("Btraj");



        BiFunction<Double,Pair<Integer,Integer>, Double> gridFunction =  (t, pair) -> Atraj[(int)Math.round(t)][pair.getFirst()][pair.getSecond()];
        SpatioTemporalSignal<Double> signal = TestUtils.createSpatioTemporalSignalFromGrid(Atraj[0].length, Atraj[0][0].length, 0, 1, Atraj.length-1, gridFunction);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("/Users/lauretta/Desktop/aTraj.storage")))) {
            oos.writeObject(Atraj);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }





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

        HashMap<String, Function<SpatialModel<Double>, DistanceStructure<Double, ?>>> distanceFunctions = new HashMap<>();
        DistanceStructure<Double, Double> readD = new DistanceStructure<>(x -> x , new DoubleDistance(), 0.0, 6.0, gridModel);
        DistanceStructure<Double, Double> escapeD = new DistanceStructure<>(x -> x , new DoubleDistance(), 6.0, 32.0*32.0, gridModel);

        distanceFunctions.put("distReach", x -> readD);
        distanceFunctions.put("distEscape", x -> escapeD);

        Formula lValue = new AtomicFormula("LowValues");
        Formula hValue = new AtomicFormula("HighValues");

        Formula or12 = new OrFormula(lValue,hValue);
        Formula notOr12 = new NegationFormula(or12);

        Formula reachF = new ReachFormula(
                new AtomicFormula("LowValues"),"ciccia", "distReach", notOr12);
        Formula negReach = new NegationFormula(reachF);

        ///////////////
        Formula escapeLow = new EscapeFormula("ciccia", "distEscape", new AtomicFormula("LowValues"));
        //Formula reach = new ReachFormula(lValue,"ciccia", "distEscape", hValue);
        ///////////////

        Formula negEsc = new NegationFormula(escapeLow);

        Formula surr = new AndFormula(new AndFormula(new AtomicFormula("LowValues"), negReach), negEsc);

        //// MONITOR /////
        SpatioTemporalMonitoring<Double, Double, Double> monitor =
                new SpatioTemporalMonitoring<>(
                        atomicFormulas,
                        distanceFunctions,
                        new DoubleDomain(),
                        true);

        SpatioTemporalMonitoring<Double, Double, Boolean> monitorB =
                new SpatioTemporalMonitoring<>(
                        atomicFormulasB,
                        distanceFunctions,
                        new BooleanDomain(),
                        true);


        BiFunction<DoubleFunction<SpatialModel<Double>>, SpatioTemporalSignal<Double>, SpatioTemporalSignal<Double>> m =
                monitor.monitor(escapeLow, null);
        SpatioTemporalSignal<Double> sout = m.apply(t -> gridModel, signal);
        List<Signal<Double>> signals = sout.getSignals();

        System.out.println(signals.get(0));
    }



}