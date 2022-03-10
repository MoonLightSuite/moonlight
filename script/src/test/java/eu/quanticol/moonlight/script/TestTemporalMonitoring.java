package eu.quanticol.moonlight.script;

import eu.quanticol.moonlight.MoonLightTemporalScript;
import eu.quanticol.moonlight.TemporalScriptComponent;
import eu.quanticol.moonlight.domain.BooleanDomain;
import eu.quanticol.moonlight.core.formula.Interval;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.offline.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.core.base.DataHandler;
import eu.quanticol.moonlight.core.base.MoonLightRecord;
import eu.quanticol.moonlight.offline.signal.RecordHandler;
import eu.quanticol.moonlight.offline.signal.Signal;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class TestTemporalMonitoring {

    public final static String CODE_SINGLE_ATOMIC = "signal { real x;\n" +
            " real y;\n " +
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
            "formula notXY = !(x<0)\n;"
    ;


    private final BooleanDomain booleanDomain = new BooleanDomain();
    private final TemporalMonitor<MoonLightRecord,Boolean> lessThanZeroX =
            TemporalMonitor.atomicMonitor(r -> booleanDomain.computeLessThan(r.getDoubleOf(0),0));
    private final TemporalMonitor<MoonLightRecord,Boolean> lessThanZeroY =
            TemporalMonitor.atomicMonitor(r -> booleanDomain.computeLessThan(r.getDoubleOf(1),0));
    private final TemporalMonitor<MoonLightRecord,Boolean> eventuallyLessThanZeroX =
            TemporalMonitor.eventuallyMonitor(lessThanZeroX, booleanDomain);
    private final TemporalMonitor<MoonLightRecord,Boolean> eventuallyLessThanZeroY =
            TemporalMonitor.eventuallyMonitor(lessThanZeroY, booleanDomain);
    private final TemporalMonitor<MoonLightRecord,Boolean> eventuallyBLessThanZeroX =
            TemporalMonitor.eventuallyMonitor(lessThanZeroX, booleanDomain, new Interval(0,10));
    private final TemporalMonitor<MoonLightRecord,Boolean> eventuallyBLessThanZeroY =
            TemporalMonitor.eventuallyMonitor(lessThanZeroY, booleanDomain, new Interval(0,10));
    private final TemporalMonitor<MoonLightRecord,Boolean> globallyLessThanZeroX =
            TemporalMonitor.globallyMonitor(lessThanZeroX, booleanDomain);
    private final TemporalMonitor<MoonLightRecord,Boolean> globallyLessThanZeroY =
            TemporalMonitor.globallyMonitor(lessThanZeroY, booleanDomain);
    private final TemporalMonitor<MoonLightRecord,Boolean> globallyBLessThanZeroX =
            TemporalMonitor.globallyMonitor(lessThanZeroX, booleanDomain, new Interval(0,10));
    private final TemporalMonitor<MoonLightRecord,Boolean> globallyBLessThanZeroY =
            TemporalMonitor.globallyMonitor(lessThanZeroY, booleanDomain, new Interval(0,10));
    private final TemporalMonitor<MoonLightRecord,Boolean> onceLessThanZeroX =
            TemporalMonitor.onceMonitor(lessThanZeroX, booleanDomain);
    private final TemporalMonitor<MoonLightRecord,Boolean> onceLessThanZeroY =
            TemporalMonitor.onceMonitor(lessThanZeroY, booleanDomain);
    private final TemporalMonitor<MoonLightRecord,Boolean> onceBLessThanZeroX =
            TemporalMonitor.onceMonitor(lessThanZeroX, booleanDomain, new Interval(0,10));
    private final TemporalMonitor<MoonLightRecord,Boolean> onceBLessThanZeroY =
            TemporalMonitor.onceMonitor(lessThanZeroY, booleanDomain, new Interval(0,10));
    private final TemporalMonitor<MoonLightRecord,Boolean> historicallyLessThanZeroX =
            TemporalMonitor.historicallyMonitor(lessThanZeroX, booleanDomain);
    private final TemporalMonitor<MoonLightRecord,Boolean> historicallyLessThanZeroY =
            TemporalMonitor.historicallyMonitor(lessThanZeroY, booleanDomain);
    private final TemporalMonitor<MoonLightRecord,Boolean> historicallyBLessThanZeroX =
            TemporalMonitor.historicallyMonitor(lessThanZeroX, booleanDomain, new Interval(0,10));
    private final TemporalMonitor<MoonLightRecord,Boolean> historicallyBLessThanZeroY =
            TemporalMonitor.historicallyMonitor(lessThanZeroY, booleanDomain, new Interval(0,10));
    private final TemporalMonitor<MoonLightRecord,Boolean> untilXY =
            TemporalMonitor.untilMonitor(lessThanZeroX, lessThanZeroY, booleanDomain);
    private final TemporalMonitor<MoonLightRecord,Boolean> untilBXY =
            TemporalMonitor.untilMonitor(lessThanZeroX, new Interval(0,10), lessThanZeroY, booleanDomain);
    private final TemporalMonitor<MoonLightRecord,Boolean> sinceXY =
            TemporalMonitor.sinceMonitor(lessThanZeroX, lessThanZeroY, booleanDomain);
    private final TemporalMonitor<MoonLightRecord,Boolean> sinceBXY =
            TemporalMonitor.sinceMonitor(lessThanZeroX, new Interval(0,10), lessThanZeroY, booleanDomain);
    private final TemporalMonitor<MoonLightRecord,Boolean> andXY =
            TemporalMonitor.andMonitor(lessThanZeroX, booleanDomain, lessThanZeroY);
    private final TemporalMonitor<MoonLightRecord,Boolean> orXY =
            TemporalMonitor.orMonitor(lessThanZeroX, booleanDomain, lessThanZeroY);
    private final TemporalMonitor<MoonLightRecord,Boolean> impliesXY =
            TemporalMonitor.impliesMonitor(lessThanZeroX, booleanDomain, lessThanZeroY);
    private final TemporalMonitor<MoonLightRecord,Boolean> notXY =
            TemporalMonitor.notMonitor(lessThanZeroX, booleanDomain);

    private final RecordHandler recordHandler = new RecordHandler(DataHandler.REAL, DataHandler.REAL);

    private static MoonLightTemporalScript getMonitor(String code) throws IOException, MoonLightScriptLoaderException {
        return ScriptLoader.loaderFromCode(code).getScript().temporal();
    }

    public static double[] getTimePoints(double start, double dt, int length) {
        return IntStream.range(0,length).mapToDouble(i -> start+dt*i).toArray();
    }

    public static double[] getValues(DoubleUnaryOperator op, double[] timeSteps) {
        return DoubleStream.of( timeSteps ).map( op ).toArray();
    }

    private void compareResults(TemporalScriptComponent<?> t, TemporalMonitor<MoonLightRecord, Boolean> lessThanZeroX, double[] timePoints, double[][] values, BooleanDomain booleanDomain) {
        double[][] x = t.monitorToArray(timePoints,values);
        double[][] expected = doMonitor(lessThanZeroX,timePoints,values,booleanDomain);
        assertSameResults(expected,x);
    }


    @Test
    void testLessThanZeroX() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("lessThanZeroX");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, lessThanZeroX, timePoints, values, booleanDomain);
    }

    @Test
    void testLessThanZeroY() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("lessThanZeroY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, lessThanZeroY, timePoints, values, booleanDomain);
    }

    @Test
    void testEventuallyLessThanZeroX() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("eventuallyLessThanZeroX");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, eventuallyLessThanZeroX, timePoints, values, booleanDomain);
    }

    @Test
    void testEventuallyLessThanZeroY() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("eventuallyLessThanZeroY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, eventuallyLessThanZeroY, timePoints, values, booleanDomain);
    }

    @Test
    void testEventuallyBLessThanZeroX() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("eventuallyBLessThanZeroX");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, eventuallyBLessThanZeroX, timePoints, values, booleanDomain);
    }

    @Test
    void testEventuallyBLessThanZeroY() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("eventuallyBLessThanZeroY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, eventuallyBLessThanZeroY, timePoints, values, booleanDomain);
    }

    @Test
    void testGloballyLessThanZeroX() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("globallyLessThanZeroX");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, globallyLessThanZeroX, timePoints, values, booleanDomain);
    }

    @Test
    void testGloballyLessThanZeroY() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("globallyLessThanZeroY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, globallyLessThanZeroY, timePoints, values, booleanDomain);
    }

    @Test
    void testGloballyBLessThanZeroX() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("globallyBLessThanZeroX");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, globallyBLessThanZeroX, timePoints, values, booleanDomain);
    }

    @Test
    void testGloballyBLessThanZeroY() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("globallyBLessThanZeroY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, globallyBLessThanZeroY, timePoints, values, booleanDomain);
    }

    @Test
    void testOnceLessThanZeroX() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("onceLessThanZeroX");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, onceLessThanZeroX, timePoints, values, booleanDomain);
    }

    @Test
    void testOnceLessThanZeroY() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("onceLessThanZeroY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, onceLessThanZeroY, timePoints, values, booleanDomain);
    }

    @Test
    void testOnceBLessThanZeroX() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("onceBLessThanZeroX");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, onceBLessThanZeroX, timePoints, values, booleanDomain);
    }

    @Test
    void testOnceBLessThanZeroY() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("onceBLessThanZeroY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, onceBLessThanZeroY, timePoints, values, booleanDomain);
    }

    @Test
    void testHistoricallyLessThanZeroX() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("historicallyLessThanZeroX");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, historicallyLessThanZeroX, timePoints, values, booleanDomain);
    }

    @Test
    void testHistoricallyLessThanZeroY() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("historicallyLessThanZeroY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, historicallyLessThanZeroY, timePoints, values, booleanDomain);
    }

    @Test
    void testHistoricallyBLessThanZeroX() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("historicallyBLessThanZeroX");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, historicallyBLessThanZeroX, timePoints, values, booleanDomain);
    }

    @Test
    void testHistoricallyBLessThanZeroY() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("historicallyBLessThanZeroY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, historicallyBLessThanZeroY, timePoints, values, booleanDomain);
    }

    @Test
    void testUntilXY() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("untilXY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, untilXY, timePoints, values, booleanDomain);
    }

    @Test
    void testUntilBXY() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("untilBXY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, untilBXY, timePoints, values, booleanDomain);
    }

    @Test
    void testSinceXY() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("sinceXY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, sinceXY, timePoints, values, booleanDomain);
    }

    @Test
    void testSinceBXY() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("sinceBXY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, sinceBXY, timePoints, values, booleanDomain);
    }

    @Test
    void testAndXY() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("andXY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, andXY, timePoints, values, booleanDomain);
    }

    @Test
    void testOrXY() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("orXY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, orXY, timePoints, values, booleanDomain);
    }

    @Test
    void testNotXY() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("impliesXY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, impliesXY, timePoints, values, booleanDomain);
    }

    @Test
    void testImpliesXY() throws IOException, MoonLightScriptLoaderException {
        MoonLightTemporalScript script = getMonitor(CODE_SINGLE_ATOMIC);
        TemporalScriptComponent<?> t = script.selectTemporalComponent("notXY");
        double[] timePoints = getTimePoints(0,0.5,100);
        double[][] values = scenarioSinCos(timePoints);
        compareResults(t, notXY, timePoints, values, booleanDomain);
    }

    public <S> double[][] doMonitor(TemporalMonitor<MoonLightRecord,S> monitor, double[] timePoints, double[][] values, SignalDomain<S> domain) {
        Signal<MoonLightRecord> input = RecordHandler.buildTemporalSignal(recordHandler,timePoints,values);
        return monitor.monitor(input).arrayOf(domain.getDataHandler()::doubleOf);
    }




    private void assertSameResults(double[][] expected, double[][] actual) {
        assertEquals(expected.length,actual.length);
        for(int i=0; i<expected.length; i++) {
            assertArrayEquals(expected[i], actual[i]);
        }
    }

    public static double[][] scenarioSinCos(double[] time) {
        double[] xValues = getValues(Math::sin,time);
        double[] yValues = getValues(Math::cos,time);
        double[][] result = new double[time.length][2];
        IntStream.range(0,time.length).forEach(i -> {
            result[i][0] = xValues[i];
            result[i][1] = yValues[i];
        });
        return result;
    }
 }
