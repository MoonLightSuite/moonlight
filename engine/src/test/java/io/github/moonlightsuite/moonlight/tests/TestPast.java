package io.github.moonlightsuite.moonlight.tests;

import io.github.moonlightsuite.moonlight.core.formula.Formula;
import io.github.moonlightsuite.moonlight.formula.*;
import io.github.moonlightsuite.moonlight.formula.classic.NegationFormula;
import io.github.moonlightsuite.moonlight.formula.temporal.HistoricallyFormula;
import io.github.moonlightsuite.moonlight.formula.temporal.OnceFormula;
import io.github.moonlightsuite.moonlight.formula.temporal.SinceFormula;
import io.github.moonlightsuite.moonlight.io.json.IllegalFileFormat;
import io.github.moonlightsuite.moonlight.io.json.JSonTemporalSignalDeserializer;
import io.github.moonlightsuite.moonlight.offline.monitoring.TemporalMonitoring;
import io.github.moonlightsuite.moonlight.offline.monitoring.temporal.TemporalMonitor;
import io.github.moonlightsuite.moonlight.core.io.DataHandler;
import io.github.moonlightsuite.moonlight.offline.signal.RecordHandler;
import io.github.moonlightsuite.moonlight.offline.signal.Signal;
import io.github.moonlightsuite.moonlight.offline.signal.SignalCursor;
import io.github.moonlightsuite.moonlight.domain.DoubleDomain;
import io.github.moonlightsuite.moonlight.core.formula.Interval;
import io.github.moonlightsuite.moonlight.core.base.MoonLightRecord;
import io.github.moonlightsuite.moonlight.core.base.Pair;
import io.github.moonlightsuite.moonlight.util.Utils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class TestPast {


    @Test
    void testHistorically() throws IllegalFileFormat {
        //FORMULA: !H_[0,500]!(a>=0)
        Formula a = new AtomicFormula("a");
        Formula historicallyFormula = new HistoricallyFormula(a, new Interval(0, 500));
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("traceIdentity/traceLaura.json").getFile());
        try {
            RecordHandler factory = RecordHandler.createFactory(
                    new Pair<String, DataHandler<?>>("a", DataHandler.REAL),
                    new Pair<String, DataHandler<?>>("b", DataHandler.REAL)
            );
            String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
            Signal<MoonLightRecord> signal = new JSonTemporalSignalDeserializer(factory).load(contents);
            long timeInit = System.currentTimeMillis();
            HashMap<String, Function<Parameters, Function<MoonLightRecord, Double>>> mappa = new HashMap<>();
            int index_of_x = 0;
            //a is the atomic proposition: a>=0
            mappa.put("a", y -> assignment -> assignment.get(index_of_x, Double.class));
            TemporalMonitoring<MoonLightRecord, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
            TemporalMonitor<MoonLightRecord, Double> m = monitoring.monitor(historicallyFormula);
            Signal<Double> outputSignal = m.monitor(signal);
            long timeEnd = System.currentTimeMillis();
            SignalCursor<Double, MoonLightRecord> expected = signal.getIterator(true);
            SignalCursor<Double, Double> actual = outputSignal.getIterator(true);
            System.out.println(actual);
            while (!actual.isCompleted()) {
                assertFalse(expected.isCompleted());
                Double valueActual = actual.getCurrentValue();
                MoonLightRecord valueExpected = expected.getCurrentValue();
                assertEquals(valueExpected.get(0, Double.class), valueActual);
                expected.forward();
                actual.forward();
            }
            System.out.println("TIME MoonLight: " + (timeEnd - timeInit) / 1000.);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testOnce() throws IOException, IllegalFileFormat {
        double onceStart = 0.0;
        double onceEnd = 500.0;
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("traceIdentity/traceLaura.json").getFile());
        RecordHandler factory = RecordHandler.createFactory(
                new Pair<String, DataHandler<?>>("a", DataHandler.REAL),
                new Pair<String, DataHandler<?>>("b", DataHandler.REAL)
        );
        String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
        Signal<MoonLightRecord> signal = new JSonTemporalSignalDeserializer(factory).load(contents);
        Formula a = new AtomicFormula("a");
        Formula notA = new NegationFormula(a);
        Formula once = new OnceFormula(notA, new Interval(onceStart, onceEnd));
        Formula notOnceNotA = new NegationFormula(once);
        //signal
        HashMap<String, Function<Parameters, Function<MoonLightRecord, Double>>> mappa = new HashMap<>();
        int index_of_x = 0;
        //a is the atomic proposition: a>=0
        mappa.put("a", y -> assignment -> assignment.get(index_of_x, Double.class));
        TemporalMonitoring<MoonLightRecord, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
        TemporalMonitor<MoonLightRecord, Double> m = monitoring.monitor(notOnceNotA);
        Signal<Double> outputSignal = m.monitor(signal);
        SignalCursor<Double, MoonLightRecord> expected = signal.getIterator(true);
        SignalCursor<Double, Double> actual = outputSignal.getIterator(true);
        assertEquals(signal.getStart() + onceEnd, outputSignal.getStart(), 0.0);
        assertEquals(signal.getEnd(), outputSignal.getEnd(), 0.0);
        while (!actual.isCompleted()) {
            assertFalse(expected.isCompleted());
            Double nextActual = actual.getCurrentValue();
            MoonLightRecord nextExpected = expected.getCurrentValue();
            double time = expected.getCurrentTime();
//                if (time > 500) {
//                    break;
//                }
            assertEquals(nextExpected.get(0, Double.class), nextActual, "Time: " + time);
            actual.forward();
            expected.forward();
        }
    }

    @Test
    void testOnce2() {
        Signal<Double> signal = Utils.createSignal(0.0, 10.0, 0.1, x -> x);
        Formula once = new OnceFormula(new AtomicFormula("test"), new Interval(0, 5.0));
        TemporalMonitoring<Double, Double> monitoring = new TemporalMonitoring<>(new DoubleDomain());
        monitoring.addProperty("test", p -> (x -> x));
        TemporalMonitor<Double, Double> m = monitoring.monitor(once);
        Signal<Double> result = m.monitor(signal);
        assertEquals(signal.getEnd(), result.getEnd(), 0.0);
        assertEquals(5.0, result.getStart(), 0.0);
        SignalCursor<Double, Double> c = result.getIterator(true);
        double time = 5.0;
        while (!c.isCompleted()) {
            assertEquals(c.getCurrentTime(), c.getCurrentValue(), 0.0000001);
            c.forward();
            time += 0.1;
        }
        assertTrue(time > 10.0);
    }

    @Test
    void testHistorically2() {
        Signal<Double> signal = Utils.createSignal(0.0, 10.0, 0.25, x -> x);
        Formula historically = new HistoricallyFormula(new AtomicFormula("test"), new Interval(0, 5.0));
        TemporalMonitoring<Double, Double> monitoring = new TemporalMonitoring<>(new DoubleDomain());
        monitoring.addProperty("test", p -> (x -> x));
        TemporalMonitor<Double, Double> m = monitoring.monitor(historically);
        Signal<Double> result = m.monitor(signal);
        assertEquals(signal.getEnd(), result.getEnd(), 0.0);
        assertEquals(5.0, result.getStart(), 0.0);
        SignalCursor<Double, Double> c = result.getIterator(true);
        double time = 5.0;
        while (!c.isCompleted()) {
            assertEquals(c.getCurrentTime() - 5.0, c.getCurrentValue(), 0.0, "Time: " + c.getCurrentTime());
            c.forward();
            time += 0.25;
        }
        assertEquals(10.25, time, 0.0);
    }

    @Test
    void testSince() {
        Signal<Double> signal = Utils.createSignal(0.0, 10.0, 0.25, x -> x);
        Formula since = new SinceFormula(new AtomicFormula("test1"), new AtomicFormula("test2"), new Interval(0, 5.0));
        TemporalMonitoring<Double, Double> monitoring = new TemporalMonitoring<>(new DoubleDomain());
        monitoring.addProperty("test1", p -> (x -> x));
        monitoring.addProperty("test2", p -> (x -> x - 9));
        TemporalMonitor<Double, Double> m = monitoring.monitor(since);
        Signal<Double> result = m.monitor(signal);
        assertEquals(signal.getEnd(), result.getEnd(), 0.0);
        assertEquals(5.0, result.getStart(), 0.0);
        SignalCursor<Double, Double> c = result.getIterator(true);
        double time = 5.0;
        while (!c.isCompleted()) {
            assertEquals(c.getCurrentTime() - 9.0, c.getCurrentValue(), 0.0, "Time: " + c.getCurrentTime());
            c.forward();
            time += 0.25;
        }
        assertEquals(10.25, time, 0.0);
    }

    @Test
    void testUnboundedSince() {
        Signal<Double> signal = Utils.createSignal(0.0, 10.0, 0.25, x -> x);
        Formula since = new SinceFormula(new AtomicFormula("test1"), new AtomicFormula("test2"));
        TemporalMonitoring<Double, Double> monitoring = new TemporalMonitoring<>(new DoubleDomain());
        monitoring.addProperty("test1", p -> (x -> x));
        monitoring.addProperty("test2", p -> (x -> x - 9));
        TemporalMonitor<Double, Double> m = monitoring.monitor(since);
        Signal<Double> result = m.monitor(signal);
        assertEquals(signal.getEnd(), result.getEnd(), 0.0);
        assertEquals(0.0, result.getStart(), 0.0);
        SignalCursor<Double, Double> c = result.getIterator(true);
        double time = 0.0;
        while (!c.isCompleted()) {
            assertEquals(c.getCurrentTime() - 9.0, c.getCurrentValue(), 0.0, "Time: " + c.getCurrentTime());
            c.forward();
            time += 0.25;
        }
        assertEquals(10.25, time, 0.0);
    }


    @Test
    void testHistoricallyFailsInternally() {
        Signal<Double> signal = Utils.createSignal(0.0, 0.4, 0.1, x -> x);
        // errore per  Interval(0.1, 0.3)
        Formula historically = new HistoricallyFormula(new AtomicFormula("test"), new Interval(0.1, 0.3));
        TemporalMonitoring<Double, Double> monitoring = new TemporalMonitoring<>(new DoubleDomain());
        monitoring.addProperty("test", p -> (x -> x));
        TemporalMonitor<Double, Double> m = monitoring.monitor(historically);
        Signal<Double> result = m.monitor(signal);
        // <3.1000000000000014>
        assertEquals(signal.getEnd(), result.getEnd(), 0.0);
        // dovrebbe partire da 0.1
        assertEquals(0.3, result.getStart(), 0.0);
    }

    @Test
    void testFormulasProduceASignalOfWrongLength() {
        testHistoricallyFormulaProduceASignalOfRightLength(0.0, 3.0, 2.0, 3.0);
        testHistoricallyFormulaProduceASignalOfRightLength(0.0, 3.0, 2.0, 3.0);
        testOnceFormulaProduceASignalOfRightLength(0.0, 3.0, 2.0, 3.0);
        testSinceFormulaProduceASignalOfRightLength(0.0, 3.0, 2.0, 3.0);
    }

    //@Disabled("Michele per certi valori il segnale si accorcia")
    @Test
    void testFormulasProduceASignalOfRightLength() {
        assertAll(
                //() -> testAllFormulasProduceASignalOfRightLength(0.0, 0.5, 0.1, 0.3)
                () -> testAllFormulasProduceASignalOfRightLength(500, 693, 60, 116)
                //() -> testAllFormulasProduceASignalOfRightLength(0.0, 3.0, 0.0, 2.0)
                //() -> testAllFormulasProduceASignalOfRightLength(0.0, 3.0, 2.0, 3.0)
        );
    }

    void testAllFormulasProduceASignalOfRightLength(double signalLowerBound, double signalUpperBound, double formulaLowerBound, double formulaUpperBound) {
        assertAll(
                () -> testHistoricallyFormulaProduceASignalOfRightLength(signalLowerBound, signalUpperBound, formulaLowerBound, formulaUpperBound),
                () -> testOnceFormulaProduceASignalOfRightLength(signalLowerBound, signalUpperBound, formulaLowerBound, formulaUpperBound),
                () -> testSinceFormulaProduceASignalOfRightLength(signalLowerBound, signalUpperBound, formulaLowerBound, formulaUpperBound)
        );
    }

    void testHistoricallyFormulaProduceASignalOfRightLength(double signalLowerBound, double signalUpperBound, double formulaLowerBound, double formulaUpperBound) {
        double expectedOutputLowerBound = signalLowerBound + formulaUpperBound;
        double expectedOutputUpperBound = signalUpperBound;
        Function<Double, Double> atomicFormula = d -> d;
        Signal<Double> signal = Utils.createSignal(signalLowerBound, signalUpperBound, 0.1, x -> x);
        TemporalMonitor<Double, Double> monitor = TemporalMonitor.historicallyMonitor(TemporalMonitor.atomicMonitor(atomicFormula),
                new DoubleDomain(), new Interval(formulaLowerBound, formulaUpperBound));
        Signal<Double> output = monitor.monitor(signal);
        assertAll(
                () -> assertEquals(expectedOutputLowerBound, output.getStart(), 0.0, String.format("HISTORICALLY - [%s,%s] - Wrong LowerBound", expectedOutputLowerBound, expectedOutputUpperBound)),
                () -> assertEquals(expectedOutputUpperBound, output.getEnd(), 0.0, String.format("HISTORICALLY - [%s,%s] - Wrong UpperBound", expectedOutputLowerBound, expectedOutputUpperBound))

        );
    }

    void testOnceFormulaProduceASignalOfRightLength(double signalLowerBound, double signalUpperBound, double formulaLowerBound, double formulaUpperBound) {
        double expectedOutputLowerBound = signalLowerBound + formulaUpperBound;
        double expectedOutputUpperBound = signalUpperBound;// + formulaLowerBound;
        Function<Double, Double> atomicFormula = d -> d;
        Signal<Double> signal = Utils.createSignal(signalLowerBound, signalUpperBound, 0.1, x -> x);
        TemporalMonitor<Double, Double> monitor = TemporalMonitor.onceMonitor(TemporalMonitor.atomicMonitor(atomicFormula),
                new DoubleDomain(), new Interval(formulaLowerBound, formulaUpperBound));
        Signal<Double> output = monitor.monitor(signal);
        assertAll(
                () -> assertEquals(expectedOutputLowerBound, output.getStart(), 0.0, String.format("ONCE - [%s,%s] - Wrong LowerBound", expectedOutputLowerBound, expectedOutputUpperBound)),
                () -> assertEquals(expectedOutputUpperBound, output.getEnd(), 0.0, String.format("ONCE - [%s,%s] - Wrong UpperBound", expectedOutputLowerBound, expectedOutputUpperBound))

        );
    }

    void testSinceFormulaProduceASignalOfRightLength(double signalLowerBound, double signalUpperBound, double formulaLowerBound, double formulaUpperBound) {
        double expectedOutputLowerBound = signalLowerBound + formulaUpperBound;
        double expectedOutputUpperBound = signalUpperBound;// + formulaLowerBound;
        Function<Double, Double> atomicFormula = d -> d;
        Signal<Double> signal = Utils.createSignal(signalLowerBound, signalUpperBound, 0.1, x -> x);
        TemporalMonitor<Double, Double> monitor = TemporalMonitor.sinceMonitor(TemporalMonitor.atomicMonitor(atomicFormula), new Interval(formulaLowerBound, formulaUpperBound), TemporalMonitor.atomicMonitor(atomicFormula), new DoubleDomain());
        Signal<Double> output = monitor.monitor(signal);
        assertAll(
                () -> assertEquals(expectedOutputLowerBound, output.getStart(), 0.0, String.format("SINCE - [%s,%s] - Wrong LowerBound", expectedOutputLowerBound, expectedOutputUpperBound)),
                () -> assertEquals(expectedOutputUpperBound, output.getEnd(), 0.0, String.format("SINCE - [%s,%s] - Wrong UpperBound", expectedOutputLowerBound, expectedOutputUpperBound))

        );
    }

    @Test
    void testOnceFormulaFederico() {
        double[] data = new double[] {78.14615476784513, 78.14615476784513, 78.14615476784513, 49.32833677512347, 78.14615476784513, 78.14615476784513, 15.088930843502473, 13.600775161732201, 78.14615476784513, 78.14615476784513, 78.14615476784513, 10.225363954402809, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 53.84903640734902, 78.14615476784513, 78.14615476784513, 17.35050134722348, 78.14615476784513, 78.14615476784513, 11.320109628444415, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 13.57040798944532, 78.14615476784513, 78.14615476784513, 12.543275489281049, 37.60403715028482, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 31.75348812335425, 78.14615476784513, 22.48618936147245, 78.14615476784513, 78.14615476784513, 15.426093867210874, 78.14615476784513, 78.14615476784513, 78.14615476784513, 14.98326593236601, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 13.732347941994501, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 14.044184169968752, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 11.450306589781787, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 13.062792963221874, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 15.885603702724088, 78.14615476784513, 78.14615476784513, 24.21713641205337, 78.14615476784513, 78.14615476784513, 14.787016365717582, 78.14615476784513, 78.14615476784513, 78.14615476784513, 13.434537580430526, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 12.374053377935622, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513, 13.526585452360093, 17.28258736416524, 78.14615476784513, 78.14615476784513, 12.585073579443236, 78.14615476784513, 78.14615476784513, 78.14615476784513, 78.14615476784513};
        HashMap<Double, Double> dataMap = new HashMap<>();
        for (int i=500; i <= 693; ++i) dataMap.put((double)i, data[i - 500]);
        Signal<Double> signal = Utils.createSignal(500.0, 693.0, 1, dataMap::get);
        TemporalMonitor<Double, Double> mQ2 = TemporalMonitor.onceMonitor(
                TemporalMonitor.notMonitor(TemporalMonitor.atomicMonitor(x -> x - 2.8E9), new DoubleDomain()), new DoubleDomain(), new Interval(60, 116));
        Signal<Double> soutQ2 = mQ2.monitor(signal);
        double expectedOutputUpperBound = 693;
        assertEquals(expectedOutputUpperBound, soutQ2.getEnd(), 0.0);
    }

    @Test
    void testHistoricallyWrongSimpleLaura() {
        Signal<Double> signal = Utils.createSignal(0.0, 0.5, 0.1, x -> x);
        Formula historically = new HistoricallyFormula(new AtomicFormula("test"), new Interval(0.1, 0.3));
        TemporalMonitoring<Double, Double> monitoring = new TemporalMonitoring<>(new DoubleDomain());
        monitoring.addProperty("test", p -> (x -> x));
        TemporalMonitor<Double, Double> m = monitoring.monitor(historically);
        Signal<Double> result = m.monitor(signal);
        assertEquals(signal.getEnd(), result.getEnd(), 0.0);
    }


    @Test
    void testTimeAtTheEndOfMonitoredSignalWithOnceOperator() {
        int start = 0;
        int end = 199;

        // Questi array corrispondono alle variabili di due segnali fallaci
        double[] data1 = new double[] {6.0, 1.0, 5.0, 5.0, 5.0, 3.0, 6.0, 2.0, 4.0, 6.0, 5.0, 6.0, 2.0, 7.0, 1.0, 4.0, 2.0, 3.0, 6.0, 3.0, 4.0, 3.0, 7.0, 1.0, 2.0, 7.0, 4.0, 2.0, 5.0, 7.0, 4.0, 4.0, 4.0, 1.0, 2.0, 4.0, 6.0, 5.0, 2.0, 2.0, 4.0, 2.0, 4.0, 5.0, 7.0, 3.0, 2.0, 4.0, 3.0, 1.0, 4.0, 2.0, 4.0, 5.0, 4.0, 2.0, 4.0, 6.0, 6.0, 3.0, 2.0, 6.0, 1.0, 2.0, 6.0, 1.0, 3.0, 6.0, 1.0, 4.0, 2.0, 4.0, 3.0, 4.0, 4.0, 2.0, 7.0, 5.0, 2.0, 2.0, 4.0, 2.0, 2.0, 7.0, 5.0, 4.0, 4.0, 5.0, 6.0, 1.0, 4.0, 6.0, 6.0, 4.0, 5.0, 2.0, 4.0, 4.0, 4.0, 6.0, 6.0, 5.0, 5.0, 6.0, 4.0, 3.0, 2.0, 3.0, 5.0, 7.0, 5.0, 5.0, 5.0, 2.0, 7.0, 4.0, 6.0, 7.0, 2.0, 5.0, 6.0, 2.0, 5.0, 1.0, 6.0, 6.0, 7.0, 5.0, 5.0, 6.0, 3.0, 3.0, 7.0, 5.0, 5.0, 3.0, 5.0, 6.0, 5.0, 5.0, 4.0, 4.0, 1.0, 7.0, 4.0, 4.0, 1.0, 6.0, 5.0, 4.0, 3.0, 2.0, 5.0, 3.0, 7.0, 4.0, 4.0, 4.0, 3.0, 4.0, 5.0, 6.0, 2.0, 1.0, 1.0, 6.0, 5.0, 5.0, 2.0, 3.0, 6.0, 4.0, 4.0, 1.0, 7.0, 3.0, 6.0, 6.0, 6.0, 5.0, 5.0, 2.0, 6.0, 2.0, 4.0, 5.0, 5.0, 2.0, 1.0, 2.0, 1.0, 4.0, 5.0, 6.0, 5.0, 6.0, 6.0, 4.0, 7.0, 3.0};
        boolean[] data2 = new boolean[] {false, false, true, true, false, false, false, true, false, false, false, true, false, false, true, false, false, false, false, true, false, false, false, true, false, true, false, false, true, true, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, true, false, false, false, true, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, true, false, false, false, false, false, false, true, false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, false, true, false, false, false, false, false, false, false, true, true, false, false, false, false, false, true, false, false, true, true, true, false, false, false, false, false, true, false, true, false, false, true, true, false, false, false, true, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};

        // ignorare questa parte di boiler-plate code
        HashMap<Double, Double> dataMap1 = new HashMap<>();
        for (int i=start; i <= end; ++i) dataMap1.put((double)i, data1[i - start]);
        HashMap<Double, Double> dataMap2 = new HashMap<>();
        for (int i=start; i <= end; ++i) dataMap2.put((double)i, (!data2[i - start]) ? 0.0 : 1.0);

        // segnale su [0.0, 199.0] con le variabili di sopra
        Signal<Pair<Double, Double>> signal = Utils.createSignal((double) start, (double) end, 1.0, x -> new Pair<>(dataMap1.get(x), dataMap2.get(x)));

        // TemporalMonitor corrispondente a:
        //⃟ I=[60.0, 84.0]
        //└──Lane_ID > 72.0
        TemporalMonitor<Pair<Double, Double>, Double> mQ1 = TemporalMonitor.onceMonitor(
                TemporalMonitor.atomicMonitor(x -> x.getFirst() - 72.0), new DoubleDomain(), new Interval(60, 84));
        // monitoraggio e risultati
        Signal<Double> soutQ2 = mQ1.monitor(signal);
        assertEquals(end,soutQ2.getEnd(),0.0000001);
    }

    @Test
    void testTimeAtTheEndOfMonitoredSignalWithHistoricallyOperator() {
        int start = 0;
        int end = 199;

        // Questi array corrispondono alle variabili di due segnali fallaci
        double[] data1 = new double[] {6.0, 1.0, 5.0, 5.0, 5.0, 3.0, 6.0, 2.0, 4.0, 6.0, 5.0, 6.0, 2.0, 7.0, 1.0, 4.0, 2.0, 3.0, 6.0, 3.0, 4.0, 3.0, 7.0, 1.0, 2.0, 7.0, 4.0, 2.0, 5.0, 7.0, 4.0, 4.0, 4.0, 1.0, 2.0, 4.0, 6.0, 5.0, 2.0, 2.0, 4.0, 2.0, 4.0, 5.0, 7.0, 3.0, 2.0, 4.0, 3.0, 1.0, 4.0, 2.0, 4.0, 5.0, 4.0, 2.0, 4.0, 6.0, 6.0, 3.0, 2.0, 6.0, 1.0, 2.0, 6.0, 1.0, 3.0, 6.0, 1.0, 4.0, 2.0, 4.0, 3.0, 4.0, 4.0, 2.0, 7.0, 5.0, 2.0, 2.0, 4.0, 2.0, 2.0, 7.0, 5.0, 4.0, 4.0, 5.0, 6.0, 1.0, 4.0, 6.0, 6.0, 4.0, 5.0, 2.0, 4.0, 4.0, 4.0, 6.0, 6.0, 5.0, 5.0, 6.0, 4.0, 3.0, 2.0, 3.0, 5.0, 7.0, 5.0, 5.0, 5.0, 2.0, 7.0, 4.0, 6.0, 7.0, 2.0, 5.0, 6.0, 2.0, 5.0, 1.0, 6.0, 6.0, 7.0, 5.0, 5.0, 6.0, 3.0, 3.0, 7.0, 5.0, 5.0, 3.0, 5.0, 6.0, 5.0, 5.0, 4.0, 4.0, 1.0, 7.0, 4.0, 4.0, 1.0, 6.0, 5.0, 4.0, 3.0, 2.0, 5.0, 3.0, 7.0, 4.0, 4.0, 4.0, 3.0, 4.0, 5.0, 6.0, 2.0, 1.0, 1.0, 6.0, 5.0, 5.0, 2.0, 3.0, 6.0, 4.0, 4.0, 1.0, 7.0, 3.0, 6.0, 6.0, 6.0, 5.0, 5.0, 2.0, 6.0, 2.0, 4.0, 5.0, 5.0, 2.0, 1.0, 2.0, 1.0, 4.0, 5.0, 6.0, 5.0, 6.0, 6.0, 4.0, 7.0, 3.0};
        boolean[] data2 = new boolean[] {false, false, true, true, false, false, false, true, false, false, false, true, false, false, true, false, false, false, false, true, false, false, false, true, false, true, false, false, true, true, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, true, false, false, false, true, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, true, false, false, false, false, false, false, true, false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, false, true, false, false, false, false, false, false, false, true, true, false, false, false, false, false, true, false, false, true, true, true, false, false, false, false, false, true, false, true, false, false, true, true, false, false, false, true, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};

        // ignorare questa parte di boiler-plate code
        HashMap<Double, Double> dataMap1 = new HashMap<>();
        for (int i=start; i <= end; ++i) dataMap1.put((double)i, data1[i - start]);
        HashMap<Double, Double> dataMap2 = new HashMap<>();
        for (int i=start; i <= end; ++i) dataMap2.put((double)i, (!data2[i - start]) ? 0.0 : 1.0);

        // segnale su [0.0, 199.0] con le variabili di sopra
        Signal<Pair<Double, Double>> signal = Utils.createSignal((double) start, (double) end, 1.0, x -> new Pair<>(dataMap1.get(x), dataMap2.get(x)));

        // TemporalMonitor corrispondente a:
        // I=[24.0, 49.0]
        // └──isByGuardrail is false
        TemporalMonitor<Pair<Double, Double>, Double> mQ2 = TemporalMonitor.historicallyMonitor(
                TemporalMonitor.atomicMonitor(x -> {if (x.getSecond() == 0) {return 1.0;} else {return -1.0;}}), new DoubleDomain(), new Interval(24, 49));
        // monitoraggio ed Exception
        Signal<Double> soutQ = mQ2.monitor(signal);
        double[][] monitorValuesQ2 = soutQ.arrayOf((Double x) -> (double) x);
        assertEquals(end,soutQ.getEnd());
    }
}