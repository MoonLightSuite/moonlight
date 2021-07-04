package eu.quanticol.moonlight.script;

import eu.quanticol.moonlight.MoonLightSpatialTemporalScript;
import eu.quanticol.moonlight.SpatialTemporalScriptComponent;
import eu.quanticol.moonlight.formula.BooleanDomain;
import eu.quanticol.moonlight.formula.DoubleDistance;
import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.formula.SignalDomain;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSpatialTemporalMonitoring {

    public final static String CODE_SINGLE_ATOMIC = "signal { real x;\n" +
            " real y;\n " +
            "}\n" +
            "space { edges {" +
            " real a;\n" +
            " real b; }\n " +
            "}\n" +
            "domain boolean;\n" +
            "formula lessThanZeroX = (x < 0);\n" +
            "formula lessThanZeroY = (y < 0);\n" +
            "formula eventuallyLessThanZeroX = eventually (x < 0);\n"+
            "formula eventuallyLessThanZeroY = eventually (y < 0);\n"+
            "formula eventuallyBLessThanZeroX = eventually[0,10] (x < 0);\n"+
            "formula eventuallyBLessThanZeroY = eventually[0,10] (y < 0);\n"+
            "formula globallyLessThanZeroX = globally (x < 0);\n"+
            "formula globallyLessThanZeroY = globally (y < 0);\n"+
            "formula globallyBLessThanZeroX = globally[0,10] (x < 0);\n"+
            "formula globallyBLessThanZeroY = globally[0,10] (y < 0);\n"+
            "formula onceLessThanZeroX = once (x < 0);\n"+
            "formula onceLessThanZeroY = once (y < 0);\n"+
            "formula onceBLessThanZeroX = once[0,10] (x < 0);\n"+
            "formula onceBLessThanZeroY = once[0,10] (y < 0);\n"+
            "formula historicallyLessThanZeroX = historically (x < 0);\n"+
            "formula historicallyLessThanZeroY = historically (y < 0);\n"+
            "formula historicallyBLessThanZeroX = historically[0,10] (x < 0);\n"+
            "formula historicallyBLessThanZeroY = historically[0,10] (y < 0);\n"+
            "formula untilXY = (x<0) until (y<0)\n;"+
            "formula untilBXY = (x<0) until[0,10] (y<0)\n;"+
            "formula sinceXY = (x<0) since (y<0)\n;"+
            "formula sinceBXY = (x<0) since[0,10] (y<0)\n;"+
            "formula andXY = (x<0) & (y<0)\n;"+
            "formula orXY = (x<0) | (y<0)\n;"+
            "formula impliesXY = (x<0) -> (y<0)\n;"+
            "formula notXY = !(x<0)\n;"+
            "formula somewhereXYOne = somewhere[0,10] (x<0)\n;"+
            "formula somewhereXYTwo = somewhere(a+b) [0,10] (x<0)\n;"+
            "formula everywhereXYOne = everywhere[0,10] (x<0)\n;"+
            "formula everywhereXYTwo = everywhere(a+b) [0,10] (x<0)\n;"+
            "formula escapeXYOne = escape[0,10] (x<0)\n;"+
            "formula escapeXYTwo = escape(a+b) [0,10] (x<0)\n;"+
            "formula reachXYOne = (x<0) reach[0,10] (y<0)\n;"+
            "formula reachXYTwo = (x<0) reach(a+b) [0,10] (y<0)\n;"
    ;


    private final BooleanDomain booleanDomain = new BooleanDomain();
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> lessThanZeroX =
            SpatialTemporalMonitor.atomicMonitor(r -> booleanDomain.computeLessThan(r.getDoubleOf(0),0));
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> lessThanZeroY =
            SpatialTemporalMonitor.atomicMonitor(r -> booleanDomain.computeLessThan(r.getDoubleOf(1),0));
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> eventuallyLessThanZeroX =
            SpatialTemporalMonitor.eventuallyMonitor(lessThanZeroX, booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> eventuallyLessThanZeroY =
            SpatialTemporalMonitor.eventuallyMonitor(lessThanZeroY, booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> eventuallyBLessThanZeroX =
            SpatialTemporalMonitor.eventuallyMonitor(lessThanZeroX, new Interval(0,10), booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> eventuallyBLessThanZeroY =
            SpatialTemporalMonitor.eventuallyMonitor(lessThanZeroY, new Interval(0,10), booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> globallyLessThanZeroX =
            SpatialTemporalMonitor.globallyMonitor(lessThanZeroX, booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> globallyLessThanZeroY =
            SpatialTemporalMonitor.globallyMonitor(lessThanZeroY, booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> globallyBLessThanZeroX =
            SpatialTemporalMonitor.globallyMonitor(lessThanZeroX, new Interval(0,10), booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> globallyBLessThanZeroY =
            SpatialTemporalMonitor.globallyMonitor(lessThanZeroY, new Interval(0,10), booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> onceLessThanZeroX =
            SpatialTemporalMonitor.onceMonitor(lessThanZeroX, booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> onceLessThanZeroY =
            SpatialTemporalMonitor.onceMonitor(lessThanZeroY, booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> onceBLessThanZeroX =
            SpatialTemporalMonitor.onceMonitor(lessThanZeroX, new Interval(0,10), booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> onceBLessThanZeroY =
            SpatialTemporalMonitor.onceMonitor(lessThanZeroY, new Interval(0,10), booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> historicallyLessThanZeroX =
            SpatialTemporalMonitor.historicallyMonitor(lessThanZeroX, booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> historicallyLessThanZeroY =
            SpatialTemporalMonitor.historicallyMonitor(lessThanZeroY, booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> historicallyBLessThanZeroX =
            SpatialTemporalMonitor.historicallyMonitor(lessThanZeroX, new Interval(0,10), booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> historicallyBLessThanZeroY =
            SpatialTemporalMonitor.historicallyMonitor(lessThanZeroY, new Interval(0,10), booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> untilXY =
            SpatialTemporalMonitor.untilMonitor(lessThanZeroX, lessThanZeroY, booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> untilBXY =
            SpatialTemporalMonitor.untilMonitor(lessThanZeroX, new Interval(0,10), lessThanZeroY, booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> sinceXY =
            SpatialTemporalMonitor.sinceMonitor(lessThanZeroX, lessThanZeroY, booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> sinceBXY =
            SpatialTemporalMonitor.sinceMonitor(lessThanZeroX, new Interval(0,10), lessThanZeroY, booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> andXY =
            SpatialTemporalMonitor.andMonitor(lessThanZeroX, booleanDomain, lessThanZeroY);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> orXY =
            SpatialTemporalMonitor.orMonitor(lessThanZeroX, booleanDomain, lessThanZeroY);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> impliesXY =
            SpatialTemporalMonitor.impliesMonitor(lessThanZeroX, booleanDomain, lessThanZeroY);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> notXY =
            SpatialTemporalMonitor.notMonitor(lessThanZeroX, booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> somewhereXYOne =
            SpatialTemporalMonitor.somewhereMonitor(lessThanZeroX,m -> new DistanceStructure<>(r -> 1.0,new DoubleDistance(),0.0,10.0,m),booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> somewhereXYTwo =
            SpatialTemporalMonitor.somewhereMonitor(lessThanZeroX,m -> new DistanceStructure<>(r -> r.getDoubleOf(0)+r.getDoubleOf(1),new DoubleDistance(),0.0,10.0,m),booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> everywhereXYOne =
            SpatialTemporalMonitor.everywhereMonitor(lessThanZeroX,m -> new DistanceStructure<>(r -> 1.0,new DoubleDistance(),0.0,10.0,m),booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> everywhereXYTwo =
            SpatialTemporalMonitor.everywhereMonitor(lessThanZeroX,m -> new DistanceStructure<>(r -> r.getDoubleOf(0)+r.getDoubleOf(1),new DoubleDistance(),0.0,10.0,m),booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> escapeXYOne =
            SpatialTemporalMonitor.escapeMonitor(lessThanZeroX,m -> new DistanceStructure<>(r -> 1.0,new DoubleDistance(),0.0,10.0,m),booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> escapeXYTwo =
            SpatialTemporalMonitor.escapeMonitor(lessThanZeroX,m -> new DistanceStructure<>(r -> r.getDoubleOf(0)+r.getDoubleOf(1),new DoubleDistance(),0.0,10.0,m),booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> reachXYOne =
            SpatialTemporalMonitor.reachMonitor(lessThanZeroX,m -> new DistanceStructure<>(r -> 1.0,new DoubleDistance(),0.0,10.0,m), lessThanZeroY,booleanDomain);
    private final SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,Boolean> reachXYTwo =
            SpatialTemporalMonitor.reachMonitor(lessThanZeroX,m -> new DistanceStructure<>(r -> r.getDoubleOf(0)+r.getDoubleOf(1),new DoubleDistance(),0.0,10.0,m), lessThanZeroY,booleanDomain);

    private final RecordHandler recordHandler = new RecordHandler(DataHandler.REAL, DataHandler.REAL);
    private final RecordHandler edgeHandler = new RecordHandler(DataHandler.REAL, DataHandler.REAL);

    private static MoonLightSpatialTemporalScript getMonitor(String code) throws IOException {
        return ScriptLoader.loaderFromCode(code).getScript().spatialTemporal();
    }

    public static double[] getTimePoints(double start, double dt, int length) {
        return IntStream.range(0,length).mapToDouble(i -> start+dt*i).toArray();
    }

    public static double[] getValues(DoubleUnaryOperator op, double[] timeSteps) {
        return DoubleStream.of( timeSteps ).map( op ).toArray();
    }

    private void compareResults(SpatialTemporalScriptComponent<?> stsc, SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, Boolean> m, int locations, double[] timePoints, double[][][] graph, double[][][] values, BooleanDomain booleanDomain) {
        double[][][] x = stsc.monitorToDoubleArrayAdjacencyList(timePoints, graph, timePoints,values);
        double[][][] expected = doMonitor(m,locations, timePoints,values, graph, booleanDomain);
        assertSameResults(expected,x);
    }

    private double[][][] generateAdjacencyList(int height, int width, double[] timeSteps) {
        double[][][] result = new double[timeSteps.length][][];
        for(int i=0 ; i<timeSteps.length; i++) {
            result[i] = getGridAdjacencyList(height,width);
        }
        return result;
    }

    private int indexOf(int i, int j, int width) {
        return i*width+j;
    }

    private double[][] getGridAdjacencyList(int height, int width) {
        double[][] result = new double[(width-1)*height+(height-1)*width][];
        int counter = 0;
        for(int i=0; i<height;i++) {
            for(int j=0; j<width; j++) {
                if (j<width-1) {
                    result[counter++] = new double[] { indexOf(i,j,width), indexOf(i,j+1,width),1.0*i,1.0/j };
                }
                if (i<height-1) {
                    result[counter++] = new double[] { indexOf(i,j,width), indexOf(i+1,1,width),1.0/i,1.0*j };
                }
            }
        }
        return result;
    }

    @Test
    void testLessThanZeroX() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("lessThanZeroX");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, lessThanZeroX, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testLessThanZeroY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("lessThanZeroY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, lessThanZeroY, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testEventuallyLessThanZeroX() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("eventuallyLessThanZeroX");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, eventuallyLessThanZeroX, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testEventuallyLessThanZeroY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("eventuallyLessThanZeroY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, eventuallyLessThanZeroY, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testEventuallyBLessThanZeroX() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("eventuallyBLessThanZeroX");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, eventuallyBLessThanZeroX, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testEventuallyBLessThanZeroY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("eventuallyBLessThanZeroY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, eventuallyBLessThanZeroY, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testGloballyLessThanZeroX() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("globallyLessThanZeroX");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, globallyLessThanZeroX, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testGloballyLessThanZeroY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("globallyLessThanZeroY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, globallyLessThanZeroY, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testGloballyBLessThanZeroX() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("globallyBLessThanZeroX");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, globallyBLessThanZeroX, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testGloballyBLessThanZeroY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("globallyBLessThanZeroY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, globallyBLessThanZeroY, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testOnceLessThanZeroX() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("onceLessThanZeroX");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, onceLessThanZeroX, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testOnceLessThanZeroY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("onceLessThanZeroY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, onceLessThanZeroY, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testOnceBLessThanZeroX() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("onceBLessThanZeroX");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, onceBLessThanZeroX, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testOnceBLessThanZeroY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("onceBLessThanZeroY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, onceBLessThanZeroY, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testHistoricallyLessThanZeroX() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("historicallyLessThanZeroX");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, historicallyLessThanZeroX, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testHistoricallyLessThanZeroY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("historicallyLessThanZeroY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, historicallyLessThanZeroY, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testHistoricallyBLessThanZeroX() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("historicallyBLessThanZeroX");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, historicallyBLessThanZeroX, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testHistoricallyBLessThanZeroY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("historicallyBLessThanZeroY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, historicallyBLessThanZeroY, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testUntilXY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("untilXY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, untilXY, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testUntilBXY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("untilBXY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, untilBXY, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testSinceXY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("sinceXY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, sinceXY, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testSinceBXY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("sinceBXY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, sinceBXY, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testAndXY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("andXY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, andXY, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testOrXY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("orXY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, orXY, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testNotXY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("impliesXY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, impliesXY, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testImpliesXY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("notXY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(100,timePoints);
        double[][][] graph = generateAdjacencyList(10,10, timePoints);
        compareResults(t, notXY, 100, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testSomeWhereOneLessThanZeroY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("somewhereXYOne");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(10,timePoints);
        double[][][] graph = generateAdjacencyList(2,2, timePoints);
        compareResults(t, somewhereXYOne, 10, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testSomeWhereTwpLessThanZeroY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("somewhereXYTwo");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(10,timePoints);
        double[][][] graph = generateAdjacencyList(2,2, timePoints);
        compareResults(t, somewhereXYTwo, 10, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testEveryWhereOneLessThanZeroY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("everywhereXYOne");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(10,timePoints);
        double[][][] graph = generateAdjacencyList(2,2, timePoints);
        compareResults(t, everywhereXYOne, 10, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testEveryWhereTwoLessThanZeroY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("everywhereXYTwo");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(10,timePoints);
        double[][][] graph = generateAdjacencyList(2,2, timePoints);
        compareResults(t, everywhereXYTwo, 10, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testEscapeOneLessThanZeroY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("escapeXYOne");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(10,timePoints);
        double[][][] graph = generateAdjacencyList(2,2, timePoints);
        compareResults(t, escapeXYOne, 10, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testEscapeTwoLessThanZeroY() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("escapeXYTwo");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(10,timePoints);
        double[][][] graph = generateAdjacencyList(2,2, timePoints);
        compareResults(t, escapeXYTwo, 10, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testReachXYOne() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("reachXYOne");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(10,timePoints);
        double[][][] graph = generateAdjacencyList(2,2, timePoints);
        compareResults(t, reachXYOne, 10, timePoints, graph, values, booleanDomain);
    }

    @Test
    void testReachXYTwo() throws IOException {
        MoonLightSpatialTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        SpatialTemporalScriptComponent<?> t = script.selectSpatialTemporalComponent("reachXYTwo");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][][] values = scenarioSinCos(10,timePoints);
        double[][][] graph = generateAdjacencyList(2,2, timePoints);
        compareResults(t, reachXYTwo, 10, timePoints, graph, values, booleanDomain);
    }


    public <S> double[][][] doMonitor(SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,S> monitor,
                                      int locations,
                                      double[] timePoints,
                                      double[][][] values,
                                      double[][][] graph,
                                      SignalDomain<S> domain) {
        LocationService<MoonLightRecord> locationService = LocationService.buildLocationServiceFromAdjacencyList(locations, edgeHandler, timePoints, graph);
        SpatialTemporalSignal<MoonLightRecord> input = RecordHandler.buildSpatioTemporalSignal(locations,recordHandler,timePoints,values);
        return monitor.monitor(locationService,input).toArray(domain.getDataHandler()::doubleOf);
    }




    private void assertSameResults(double[][][] expected, double[][][] actual) {
        assertEquals(expected.length, actual.length);
        for(int i=0; i<expected.length; i++) {
            assertEquals(expected[i].length,actual[i].length);
            for(int j=0; j<expected[i].length; j++) {
                assertArrayEquals(expected[i][j], actual[i][j]);
            }
        }
    }

    public static double[][][] scenarioSinCos(int locations, double[] time) {
        double[][][] result = new double[locations][time.length][2];
        IntStream.range(0,locations).forEach(l -> {
            double[] xValues = getValues(d -> Math.sin(d+((double) l)/locations),time);
            double[] yValues = getValues(d -> Math.cos(d+((double) l)/locations),time);
            IntStream.range(0,time.length).forEach(i -> {
                result[l][i][0] = xValues[i];
                result[l][i][1] = yValues[i]; });
        });
        return result;
    }
 }
