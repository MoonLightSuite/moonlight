package eu.quanticol.moonlight.util;

import eu.quanticol.moonlight.core.base.Pair;
import eu.quanticol.moonlight.core.formula.Formula;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.formula.temporal.EventuallyFormula;
import eu.quanticol.moonlight.io.FormulaToTaliro;
import eu.quanticol.moonlight.io.json.IllegalFileFormat;
import eu.quanticol.moonlight.io.json.JSonTemporalSignalDeserializer;
import eu.quanticol.moonlight.offline.monitoring.TemporalMonitoring;
import eu.quanticol.moonlight.offline.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.core.base.MoonLightRecord;
import eu.quanticol.moonlight.offline.signal.RecordHandler;
import eu.quanticol.moonlight.offline.signal.Signal;
import eu.quanticol.moonlight.offline.signal.SignalCursor;
import eu.quanticol.moonlight.core.io.DataHandler;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.core.formula.Interval;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class FormulaGeneratorTest {

    private final FormulaToTaliro toTaliro = new FormulaToTaliro();


    @Test
    void getFutureFormulaLoop() throws IllegalFileFormat {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("traceIdentity/traceLaura.json").getFile());
        try {
        	RecordHandler factory = RecordHandler.createFactory(
            		new Pair<String,DataHandler<?>>("a",DataHandler.REAL),
            		new Pair<String,DataHandler<?>>("b",DataHandler.REAL)
            	);
        	String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
            Signal<MoonLightRecord> signal = new JSonTemporalSignalDeserializer(factory).load(contents);
            FormulaGenerator formulaGenerator = new FutureFormulaGenerator(new Random(1), signal.getEnd(), "a");
            Formula generatedFormula = formulaGenerator.getFormula(2);
            System.out.println(generatedFormula.toString());
            //System.out.println(toTaliro.toTaliro(generatedFormula));
            long timeInit = System.currentTimeMillis();
            HashMap<String, Function<Parameters, Function<MoonLightRecord, Double>>> mappa = new HashMap<>();
            int index_of_x = 0;
            //a is the atomic proposition: a>=0
            mappa.put("a", y -> assignment -> assignment.get(index_of_x, Double.class));
            TemporalMonitoring<MoonLightRecord, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
            TemporalMonitor<MoonLightRecord, Double> m = monitoring.monitor(generatedFormula);
            Signal<Double> outputSignal = m.monitor(signal);
            long timeEnd = System.currentTimeMillis();
            SignalCursor<Double, MoonLightRecord> expected = signal.getIterator(true);
            SignalCursor<Double, Double> actual = outputSignal.getIterator(true);
            while (!actual.isCompleted()) {
                assertFalse(expected.isCompleted());
                Double valueActual = actual.getCurrentValue();
                MoonLightRecord valueExpected = expected.getCurrentValue();
                // assertEquals(valueExpected.get(0, Double.class), valueActual);
                expected.forward();
                actual.forward();
            }
            System.out.println("TIME MoonLight: " + (timeEnd - timeInit) / 1000.);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getBothFormulaLoop() throws IllegalFileFormat {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("traceIdentity/traceLaura.json").getFile());
        try {
        	RecordHandler factory = RecordHandler.createFactory(
            		new Pair<String,DataHandler<?>>("a",DataHandler.REAL),
            		new Pair<String,DataHandler<?>>("b",DataHandler.REAL)
            	);
        	String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
            Signal<MoonLightRecord> signal = new JSonTemporalSignalDeserializer(factory).load(contents);
            FormulaGenerator formulaGenerator = new BothFormulaGenerator(new Random(1), signal.getEnd(), "a");
            Formula generatedFormula = formulaGenerator.getFormula(2);
            System.out.println(generatedFormula.toString());
            //System.out.println(toTaliro.toTaliro( generatedFormula ));
            long timeInit = System.currentTimeMillis();
            HashMap<String, Function<Parameters, Function<MoonLightRecord, Double>>> mappa = new HashMap<>();
            int index_of_x = 0;
            //a is the atomic proposition: a>=0
            mappa.put("a", y -> assignment -> assignment.get(index_of_x, Double.class));
            TemporalMonitoring<MoonLightRecord, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
            TemporalMonitor<MoonLightRecord, Double> m = monitoring.monitor(generatedFormula);
            Signal<Double> outputSignal = m.monitor(signal);
            long timeEnd = System.currentTimeMillis();
            SignalCursor<Double, MoonLightRecord> expected = signal.getIterator(true);
            SignalCursor<Double, Double> actual = outputSignal.getIterator(true);
            while (!actual.isCompleted()) {
                assertFalse(expected.isCompleted());
                Double valueActual = actual.getCurrentValue();
                MoonLightRecord valueExpected = expected.getCurrentValue();
                // assertEquals(valueExpected.get(0, Double.class), valueActual);
                expected.forward();
                actual.forward();
            }
            System.out.println("TIME MoonLight: " + (timeEnd - timeInit) / 1000.);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testRobustnessLaura3() throws IllegalFileFormat {
        //FORMULA: !<>_[0,500]!(a>=0)
        //TALIRO: //
        //BREACH: //
        Formula a = new AtomicFormula("a");
//        Formula notA = new NegationFormula(a);
        Formula eventually = new EventuallyFormula(a, new Interval(0, 1500));
//        Formula notEventuallyNotA = new NegationFormula(eventually);
        //signal
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("traceIdentity/traceLaura.json").getFile());
        try {
        	RecordHandler factory = RecordHandler.createFactory(
            		new Pair<String,DataHandler<?>>("a",DataHandler.REAL),
            		new Pair<String,DataHandler<?>>("b",DataHandler.REAL)
            	);
        	String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
            Signal<MoonLightRecord> signal = new JSonTemporalSignalDeserializer(factory).load(contents);
            HashMap<String, Function<Parameters, Function<MoonLightRecord, Double>>> mappa = new HashMap<>();
            int index_of_x = 0;
            //a is the atomic proposition: a>=0
            mappa.put("a", y -> assignment -> assignment.get(index_of_x, Double.class));
            TemporalMonitoring<MoonLightRecord, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
            TemporalMonitor<MoonLightRecord, Double> m = monitoring.monitor(eventually);
            Signal<Double> outputSignal = m.monitor(signal);
            SignalCursor<Double, MoonLightRecord> expected = signal.getIterator(true);
            SignalCursor<Double, Double> actual = outputSignal.getIterator(true);
            //assertTrue(outputSignal.end()==500.0);
            //System.out.println(outputSignal.end());
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
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

}