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
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.xtext.ScriptLoader;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

import static eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor.*;

public class EpidemicMain {
    private static final ClassLoader classLoader = EpidemicMain.class.getClassLoader();
    private static final URL  TRAJECTORY_SOURCE = classLoader.getResource("epidemic_simulation_network_0.txt");
    private static final URL  NETWORK_SOURCE = classLoader.getResource("epidemic_simulation_trajectory_0.txt");

    private static final DoubleDomain doubleDomain = new DoubleDomain();
    private static final BooleanDomain booleanDomain = new BooleanDomain();

    private static final double S = 3;    // time horizon

    private static String code = "signal { int nodeType; }\n" +
            "             	space {\n" +
            "             	edges { real length;}\n" +
            "             	}\n" +
            "             	domain boolean;\n" +
            "             	formula aFormula = somewhere [0, 1] ( nodeType==1 );";

    public static void main(String[] argv) {
        try {
            ClassLoader classLoader = EpidemicMain.class.getClassLoader();
            // space model
            File fileL = new File(TRAJECTORY_SOURCE.toURI());
            CsvLocationServiceReader readerL =  new CsvLocationServiceReader();
            RecordHandler rhL = new RecordHandler(DataHandler.REAL);
            LocationService<MoonLightRecord> ls = readerL.read(rhL, fileL);

            // trajectory
            URL resourceT = classLoader.getResource("epidemic_simulation_trajectory_0.txt");
            File fileT = new File(NETWORK_SOURCE.toURI());
            RecordHandler rhT = new RecordHandler(DataHandler.INTEGER);
            CsvSpatialTemporalSignalReader readerT = new CsvSpatialTemporalSignalReader();
            SpatialTemporalSignal<MoonLightRecord> s = readerT.load(rhT, fileT);
            SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, Boolean> m = isInfected();

            SpatialTemporalSignal<Boolean> sout = m.monitor(ls, s);
            List<Signal<Boolean>> signals = sout.getSignals();
            System.out.println(signals.get(0).valueAt(0));

            ScriptLoader sl = new ScriptLoader();
            MoonLightScript script = sl.compileScript(code);
            System.out.println( script.isSpatialTemporal() );
            MoonLightSpatialTemporalScript spatialTemporalScript = script.spatialTemporal();
            SpatialTemporalScriptComponent<?> boolMonitScript = spatialTemporalScript.selectDefaultSpatialTemporalComponent();
            SpatialTemporalSignal<?> res = boolMonitScript.getMonitor(new String[]{}).monitor(ls,s);
            //SpatialTemporalSignal<?> res = boolMonitScript.monitorFromDouble(ls, s);
            //double[][][] monitorValue = boolMonitScript.monitorToArrayFromDouble(ls, s);        } catch (Exception e) {
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    private static SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, Boolean> isInfected() {
        return SpatialTemporalMonitor.atomicMonitor(p -> p.get(0,Integer.class).intValue()== 3);
    }

    private static Function<SpatialModel<MoonLightRecord>, DistanceStructure<MoonLightRecord, ?>> distance(double from, double to) {
        return g -> new DistanceStructure<>(x -> x.get(0,Double.class).doubleValue(), new DoubleDistance(), from, to, g);
    }


//    private static Function<SpatialModel<MoonLightRecord>, DistanceStructure<MoonLightRecord, ?>> hopDistance(int from, int to) {
//        int k = 1;
//        return g -> new DistanceStructure<>( x-> hop(values), new DoubleDistance(), from, to, g);.
//    }
//
//
//    private static  int hop(MoonLightRecord values) {
//        if (values.get(0,Double.class).doubleValue() > 0) {
//            return 1;
//        }
//        return Integer.MAX_VALUE;
//    }


    private static SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, Boolean> somewhereInfected() {
        return somewhereMonitor(isInfected(),distance(0, 4),booleanDomain);
    }

    private static SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, Boolean> alwaysSomeInf() {
        return globallyMonitor(isInfected(),new Interval(0,100),booleanDomain);
    }



}
