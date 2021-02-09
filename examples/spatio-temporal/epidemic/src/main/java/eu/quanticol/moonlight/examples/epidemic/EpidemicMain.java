package eu.quanticol.moonlight.examples.epidemic;

import eu.quanticol.moonlight.MoonLightScript;
import eu.quanticol.moonlight.MoonLightSpatialTemporalScript;
import eu.quanticol.moonlight.SpatialTemporalScriptComponent;
import eu.quanticol.moonlight.formula.BooleanDomain;
import eu.quanticol.moonlight.formula.DoubleDistance;
import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.io.CsvLocationServiceReader;
import eu.quanticol.moonlight.io.CsvSpatialTemporalSignalReader;
import eu.quanticol.moonlight.io.CsvSpatialTemporalSignalWriter;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.xtext.ScriptLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor.*;

public class EpidemicMain {
    private static final File dir = new File("examples/spatio-temporal/epidemic/src/main/resources");
    private static final String cmd = "python3 exp4strel.py";
    private static final ClassLoader classLoader = EpidemicMain.class.getClassLoader();
    private static final RecordHandler rhL = new RecordHandler(DataHandler.REAL);
    private static final RecordHandler rhT = new RecordHandler(DataHandler.INTEGER);
    private static final int nRuns = 1;
    private static final List<SpatialTemporalSignal<?>>  outputs = new ArrayList<SpatialTemporalSignal<?>>();

    private static final DoubleDomain doubleDomain = new DoubleDomain();
    private static final BooleanDomain booleanDomain = new BooleanDomain();
    private static final double S = 3;    // state, S=1,E=2,I=3,R=4

    private static String code = "signal { int nodeType; }\n" +
            "             	space {\n" +
            "             	edges { real length;}\n" +
            "             	}\n" +
            "             	domain boolean;\n" +
            "             	formula aFormula = somewhere (1.0) [0, 1] ( nodeType==1 );";

    public static void main(String[] argv) {
        try {
            Runtime run = Runtime.getRuntime();
            Runtime.getRuntime().exec(cmd, null, dir);
            Process pr = run.exec(cmd);

            for(int i = 0; i <nRuns; i++) {
                // space model
                URL TRAJECTORY_SOURCE = classLoader.getResource("epidemic_simulation_network_" + i + ".txt");
                File fileL = new File(TRAJECTORY_SOURCE.toURI());
                CsvLocationServiceReader readerL = new CsvLocationServiceReader();
                LocationService<MoonLightRecord> ls = readerL.read(rhL, fileL);

                // trajectory
                URL NETWORK_SOURCE = classLoader.getResource("epidemic_simulation_trajectory_" + i + ".txt");
                File fileT = new File(NETWORK_SOURCE.toURI());
                CsvSpatialTemporalSignalReader readerT = new CsvSpatialTemporalSignalReader();
                SpatialTemporalSignal<MoonLightRecord> s = readerT.load(rhT, fileT);

                // monitor from script
                ScriptLoader sl = new ScriptLoader();
                MoonLightScript script = sl.compileScript(code);
                MoonLightSpatialTemporalScript spatialTemporalScript = script.spatialTemporal();
                spatialTemporalScript.setBooleanDomain();
                SpatialTemporalScriptComponent<?> monitScript =  spatialTemporalScript.selectSpatialTemporalComponent("aFormula");
                SpatialTemporalSignal<Boolean> results = (SpatialTemporalSignal<Boolean>) monitScript.getMonitor(new String[]{}).monitor(ls, s);
                List<? extends Signal<?>> signals = results.getSignals();
                System.out.println(signals.get(0).valueAt(0));
                outputs.add(i, results);

                URL resource = classLoader.getResource("results.txt");
                File fileResults = new File(resource.toURI());
                CsvSpatialTemporalSignalWriter writer = new CsvSpatialTemporalSignalWriter();
                writer.write(DataHandler.BOOLEAN, results,fileResults);
                String o = writer.stringOf(DataHandler.BOOLEAN,results);
                Files.write(fileResults.toPath(),o.getBytes());
                System.out.println(o);
            }


//            // monitor from function property (see below)
//            SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, Boolean> m = somewhereInfected();
//            SpatialTemporalSignal<Boolean> sout = m.monitor(ls, s);
//            List<Signal<Boolean>> signals = sout.getSignals();
//            System.out.println(signals.get(0).valueAt(0));

        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    private static SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, Boolean> isInfected() {
        return SpatialTemporalMonitor.atomicMonitor(p -> p.get(0,Integer.class).intValue()== S);
    }

    private static Function<SpatialModel<MoonLightRecord>, DistanceStructure<MoonLightRecord, ?>> distance(double from, double to) {
        return g -> new DistanceStructure<>(x -> x.get(0,Double.class).doubleValue(), new DoubleDistance(), from, to, g);
    }


    private static Function<SpatialModel<MoonLightRecord>, DistanceStructure<MoonLightRecord, ?>> hopDistance(double from, double to) {
        int k = 1;
        return g -> new DistanceStructure<>( x-> 1.0, new DoubleDistance(), from, to, g);
    }


    private static SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, Boolean> somewhereInfected() {
        return somewhereMonitor(isInfected(),distance(0, 4),booleanDomain);
    }

    private static SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, Boolean> reachInfected() {
        return somewhereMonitor(isInfected(),hopDistance(0, 4),booleanDomain);
    }

    private static SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, Boolean> alwaysSomeInf() {
        return globallyMonitor(isInfected(),new Interval(0,100),booleanDomain);
    }



}
