package eu.quanticol.moonlight.tests;

import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.io.JSonSignalReader;
import eu.quanticol.moonlight.monitoring.TemporalMonitoring;
import eu.quanticol.moonlight.signal.Assignment;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SignalCursor;
import eu.quanticol.moonlight.signal.VariableArraySignal;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.function.Function;

import static org.junit.Assert.*;

public class TestPast {
    @Test
    public void testHistorically() {
        //FORMULA: !H_[0,500]!(a>=0)
        Formula a = new AtomicFormula("a");
        Formula hystoricallyFormula  = new HystoricallyFormula(a, new Interval(0, 500));
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("traceIdentity/traceLaura.json").getFile());
        try {
            String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
            VariableArraySignal signal = JSonSignalReader.readSignal(contents);
            long timeInit = System.currentTimeMillis();
            HashMap<String, Function<Parameters, Function<Assignment, Double>>> mappa = new HashMap<>();
            int index_of_x = 0;
            //a is the atomic proposition: a>=0
            mappa.put("a", y -> assignment -> assignment.get(index_of_x, Double.class));
            TemporalMonitoring<Assignment, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
            Function<Signal<Assignment>, Signal<Double>> m = monitoring.monitor(hystoricallyFormula, null);
            Signal<Double> outputSignal = m.apply(signal);
            long timeEnd = System.currentTimeMillis();
            SignalCursor<Assignment> expected = signal.getIterator(true);
            SignalCursor<Double> actual = outputSignal.getIterator(true);
            while (!actual.completed()) {
                assertFalse(expected.completed());
                Double valueActual = actual.value();
                Assignment valueExpected = expected.value();
                assertEquals(valueExpected.get(0, Double.class), valueActual);
                expected.forward();
                actual.forward();
            }
            System.out.println("TIME MoonLight: " +(timeEnd-timeInit)/1000.);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }
    @Test
    public void testOnce() {
    	double onceStart = 0.0;
    	double onceEnd = 500.0;
        Formula a = new AtomicFormula("a");
        Formula notA = new NegationFormula(a);
        Formula once = new OnceFormula(notA, new Interval(onceStart, onceEnd));
        Formula notOnceNotA = new NegationFormula(once);
        //signal
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("traceIdentity/traceLaura.json").getFile());
        try {
            String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
            VariableArraySignal signal = JSonSignalReader.readSignal(contents);
            HashMap<String, Function<Parameters, Function<Assignment, Double>>> mappa = new HashMap<>();
            int index_of_x = 0;
            //a is the atomic proposition: a>=0
            mappa.put("a", y -> assignment -> assignment.get(index_of_x, Double.class));
            TemporalMonitoring<Assignment, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
            Function<Signal<Assignment>, Signal<Double>> m = monitoring.monitor(notOnceNotA, null);
            Signal<Double> outputSignal = m.apply(signal);
            SignalCursor<Assignment> expected = signal.getIterator(true);
            SignalCursor<Double> actual = outputSignal.getIterator(true);
            assertEquals(signal.start()+onceEnd,outputSignal.start(),0.0);
            assertEquals(signal.end(),outputSignal.end(),0.0);
            while (!actual.completed()) {
                assertFalse(expected.completed());
                Double nextActual = actual.value();
                Assignment nextExpected = expected.value();
                double time = expected.time();
//                if (time > 500) {
//                    break;
//                }
                assertEquals("Time: " + time, nextExpected.get(0, Double.class), nextActual);
                actual.forward();
                expected.forward();
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

}
