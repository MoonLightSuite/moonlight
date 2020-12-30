package eu.quanticol.moonlight.examples.epidemic;

import eu.quanticol.moonlight.MoonLightScript;
import eu.quanticol.moonlight.MoonLightSpatialTemporalScript;
import eu.quanticol.moonlight.SpatialTemporalScriptComponent;
import eu.quanticol.moonlight.TemporalScriptComponent;
import eu.quanticol.moonlight.io.CsvLocationServiceReader;
import eu.quanticol.moonlight.io.CsvSpatialTemporalSignalReader;
import eu.quanticol.moonlight.io.IllegalFileFormatException;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.xtext.ScriptLoader;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class EpidemicMain {
    private static final ClassLoader classLoader = EpidemicMain.class.getClassLoader();
    private static final URL  TRAJECTORY_SOURCE = classLoader.getResource("epidemic_simulation_network_0.txt");
    private static final URL  NETWORK_SOURCE = classLoader.getResource("epidemic_simulation_trajectory_0.txt");


    private static final double k = 0.4;    // time horizon

    private static String code = "signal { int nodeType; }\n" +
            "             	space {\n" +
            "             	edges { real length;}\n" +
            "             	}\n" +
            "             	domain boolean;\n" +
            "             	formula aFormula = somewhere [0, 1] ( nodeType==1 );";

    public static void main(String[] argv)  throws IllegalFileFormatException, URISyntaxException, IOException {
        ClassLoader classLoader = EpidemicMain.class.getClassLoader();
        // space model
        File fileL = new File(TRAJECTORY_SOURCE.toURI());
        CsvLocationServiceReader readerL =  new CsvLocationServiceReader();
        RecordHandler rhL = new RecordHandler(DataHandler.REAL);
        LocationService<Record> ls = readerL.read(rhL, fileL);

        // trajectory
        URL resourceT = classLoader.getResource("epidemic_simulation_trajectory_0.txt");
        File fileT = new File(NETWORK_SOURCE.toURI());
        RecordHandler rhT = new RecordHandler(DataHandler.REAL);
        CsvSpatialTemporalSignalReader readerT = new CsvSpatialTemporalSignalReader();
        SpatialTemporalSignal<Record> s = readerT.load(rhT, fileT);
        SpatialTemporalMonitor<Record, Record, Boolean> m = atleast_k();

        SpatialTemporalSignal<Boolean> sout = m.monitor(ls, s);
        List<Signal<Boolean>> signals = sout.getSignals();
        System.out.println(signals.get(0).valueAt(0));

        ScriptLoader sl = new ScriptLoader();
        MoonLightScript script = sl.compileScript(code);
        System.out.println( script.isSpatialTemporal() );
        MoonLightSpatialTemporalScript spatialTemporalScript = script.spatialTemporal();
        SpatialTemporalScriptComponent<?> boolMonitScript = spatialTemporalScript.selectDefaultSpatialTemporalComponent();
        //SpatialTemporalSignal<?> res = boolMonitScript.monitorFromDouble(ls, s);
        //double[][][] monitorValue = boolMonitScript.monitorToArrayFromDouble(ls, s);


    }

    private static SpatialTemporalMonitor<Record, Record, Boolean> atleast_k() {
        return SpatialTemporalMonitor.atomicMonitor(p -> Boolean.TRUE);
    }

}
