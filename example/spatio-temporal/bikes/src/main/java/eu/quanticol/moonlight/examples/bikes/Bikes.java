package eu.quanticol.moonlight.examples.bikes;


import eu.quanticol.jsstl.core.io.SyntaxErrorExpection;
import eu.quanticol.jsstl.core.io.TraGraphModelReader;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.SpatioTemporalMonitoring;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.util.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Bikes {

    public static void main(String[] args) throws IOException, SyntaxErrorExpection {
        GraphModel<Double> graphModel = getDoubleGraphModel("bssSpatialModel.tra");
        String trajectoryPat = Bikes.class.getResource("trajectory.tra").getPath();
        SpatioTemporalSignal<Pair<Double, Double>> spatioTemporalSignal = readTrajectory(graphModel, trajectoryPat);

        // %%%%%%%%% PROPERTY %%%%%%% //
        double Tf = 40;
        double k = 0;
        double twait = 5;
        double d = 0.3;
        double dmax = 11;
        HashMap<String, Function<Parameters, Function<Pair<Double, Double>, Boolean>>> atomicFormulas = new HashMap<>();
        atomicFormulas.put("B", p -> (x -> x.getFirst() > k));
        atomicFormulas.put("S", p -> (x -> x.getSecond()> k));


        HashMap<String, Function<SpatialModel<Double>, DistanceStructure<Double, ?>>> distanceFunctions = new HashMap<>();
        DistanceStructure<Double, Double> dist = new DistanceStructure<>(x -> x , new DoubleDistance(), 0.0, d, graphModel);
        distanceFunctions.put("dist", x -> dist);

        Formula Batom = new AtomicFormula("B");
        Formula Satom = new AtomicFormula("S");

        Formula somewhereB = new SomewhereFormula("dist", Batom);
        Formula somewhereS = new SomewhereFormula("dist", Satom);
        Formula phid0 = new AndFormula(somewhereB,somewhereS);

        Formula phi1 = new GloballyFormula(phid0,new Interval(0,Tf));


        //// MONITOR /////
        SpatioTemporalMonitoring<Double, Pair<Double,Double>, Boolean> monitor =
                new SpatioTemporalMonitoring<>(
                        atomicFormulas,
                        distanceFunctions,
                        new BooleanDomain(),
                        true);


        BiFunction<DoubleFunction<SpatialModel<Double>>, SpatioTemporalSignal<Pair<Double, Double>>, SpatioTemporalSignal<Boolean>> m =
                monitor.monitor(phi1, null);
        SpatioTemporalSignal<Boolean> sout = m.apply(t -> graphModel, spatioTemporalSignal);
        List<Signal<Boolean>> signals = sout.getSignals();
        System.out.println(signals.get(0).valueAt(0));
    }

    private static GraphModel<Double> getDoubleGraphModel(String path) throws IOException, SyntaxErrorExpection {
        String graphPath = Bikes.class.getResource(path).getPath();
        eu.quanticol.jsstl.core.space.GraphModel graph = new TraGraphModelReader().read(graphPath);
        graph.dMcomputation();
        GraphModel<Double> newGraphModel = new GraphModel<>(graph.getNumberOfLocations());
        graph.getEdges().forEach(s -> newGraphModel.add(s.lStart.getPosition(), s.weight, s.lEnd.getPosition()));
        return newGraphModel;
    }

    private static SpatioTemporalSignal<Pair<Double, Double>> readTrajectory(GraphModel<Double> graph, String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            List<String> strings = new ArrayList<>();
            String line = null;
            while ((line = br.readLine()) != null) {
                strings.add(line);
            }
            int timeSize = strings.size() - 2;
            int locSize = (strings.get(0).split("\t\t").length - 1) / 2;
            double[][][] data = new double[locSize][timeSize][2];
            double[] time = new double[strings.size()];
            for (int t = 0; t < strings.size() - 1; t++) {
                String[] splitted = strings.get(t + 1).split("\t\t");
                time[t] = Double.parseDouble(splitted[0]);
            }

            int timeLength = time.length;
            for (int i = 1; i < timeLength; i++) {
                if (time[i] == 0) {
                    timeLength = i;
                }
            }
            final double[] times = new double[timeLength];
            for (int t = 0; t < timeLength; t++) {
                times[t] = time[t];
            }

            for (int t = 0; t < timeLength; t++) {
                String[] splitted = strings.get(t + 1).split("\t\t");
                for (int i = 0; i < locSize; i++) {
                    data[i][t][0] = Double.parseDouble(splitted[i + 1]);
                    data[i][t][1] = Double.parseDouble(splitted[i + 1 + locSize]);
                }
            }

            SpatioTemporalSignal<Pair<Double, Double>> pairSpatioTemporalSignal = new SpatioTemporalSignal<>(graph.size());
            for (int i = 0; i < times.length; i++) {
                Integer index = i;
                double t = times[i];
                List<Pair<Double, Double>> collect = IntStream.range(0, graph.size()).mapToObj(s -> new Pair<>(data[s][index][0], data[s][index][1])).collect(Collectors.toList());
                pairSpatioTemporalSignal.add(t, collect);
            }
            return pairSpatioTemporalSignal;
        }
    }

}
