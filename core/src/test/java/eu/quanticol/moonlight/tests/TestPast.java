package eu.quanticol.moonlight.tests;

import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.io.json.Deserializer;
import eu.quanticol.moonlight.monitoring.TemporalMonitoring;
import eu.quanticol.moonlight.signal.Assignment;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SignalCursor;
import eu.quanticol.moonlight.signal.VariableArraySignal;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class TestPast {


    private VariableArraySignal load(String name) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource(name).getFile());
        String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
        return Deserializer.VARIABLE_ARRAY_SIGNAL.deserialize(contents);
    }

    @Test
    void testHistorically() {
        //FORMULA: !H_[0,500]!(a>=0)
        Formula a = new AtomicFormula("a");
        Formula hystoricallyFormula = new HystoricallyFormula(a, new Interval(0, 500));
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("traceIdentity/traceLaura.json").getFile());
        try {
            String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
            VariableArraySignal signal = Deserializer.VARIABLE_ARRAY_SIGNAL.deserialize(contents);
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
            System.out.println("TIME MoonLight: " + (timeEnd - timeInit) / 1000.);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testOnce() throws IOException {
        double onceStart = 0.0;
        double onceEnd = 500.0;
        VariableArraySignal signal = load("traceIdentity/traceLaura.json");
        Formula a = new AtomicFormula("a");
        Formula notA = new NegationFormula(a);
        Formula once = new OnceFormula(notA, new Interval(onceStart, onceEnd));
        Formula notOnceNotA = new NegationFormula(once);
        //signal
        HashMap<String, Function<Parameters, Function<Assignment, Double>>> mappa = new HashMap<>();
        int index_of_x = 0;
        //a is the atomic proposition: a>=0
        mappa.put("a", y -> assignment -> assignment.get(index_of_x, Double.class));
        TemporalMonitoring<Assignment, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
        Function<Signal<Assignment>, Signal<Double>> m = monitoring.monitor(notOnceNotA, null);
        Signal<Double> outputSignal = m.apply(signal);
        SignalCursor<Assignment> expected = signal.getIterator(true);
        SignalCursor<Double> actual = outputSignal.getIterator(true);
        assertEquals(signal.start() + onceEnd, outputSignal.start(), 0.0);
        assertEquals(signal.end(), outputSignal.end(), 0.0);
        while (!actual.completed()) {
            assertFalse(expected.completed());
            Double nextActual = actual.value();
            Assignment nextExpected = expected.value();
            double time = expected.time();
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
        Signal<Double> signal = TestUtils.createSignal(0.0, 10.0, 0.1, x -> x);
        Formula once = new OnceFormula(new AtomicFormula("test"), new Interval(0, 5.0));
        TemporalMonitoring<Double, Double> monitoring = new TemporalMonitoring<>(new DoubleDomain());
        monitoring.addProperty("test", p -> (x -> x));
        Function<Signal<Double>, Signal<Double>> m = monitoring.monitor(once, null);
        Signal<Double> result = m.apply(signal);
        assertEquals(signal.end(), result.end(), 0.0);
        assertEquals(5.0, result.start(), 0.0);
        SignalCursor<Double> c = result.getIterator(true);
        double time = 5.0;
        while (!c.completed()) {
            assertEquals(c.time(), c.value(), 0.0000001);
            c.forward();
            time += 0.1;
        }
        assertTrue(time > 10.0);
    }

    @Test
    void testHistorically2() {
        Signal<Double> signal = TestUtils.createSignal(0.0, 10.0, 0.25, x -> x);
        Formula historically = new HystoricallyFormula(new AtomicFormula("test"), new Interval(0, 5.0));
        TemporalMonitoring<Double, Double> monitoring = new TemporalMonitoring<>(new DoubleDomain());
        monitoring.addProperty("test", p -> (x -> x));
        Function<Signal<Double>, Signal<Double>> m = monitoring.monitor(historically, null);
        Signal<Double> result = m.apply(signal);
        assertEquals(signal.end(), result.end(), 0.0);
        assertEquals(5.0, result.start(), 0.0);
        SignalCursor<Double> c = result.getIterator(true);
        double time = 5.0;
        while (!c.completed()) {
            assertEquals(c.time() - 5.0, c.value(), 0.0, "Time: " + c.time());
            c.forward();
            time += 0.25;
        }
        assertEquals(10.25, time, 0.0);
    }

    @Test
    void testSince() {
        Signal<Double> signal = TestUtils.createSignal(0.0, 10.0, 0.25, x -> x);
        Formula since = new SinceFormula(new AtomicFormula("test1"), new AtomicFormula("test2"), new Interval(0, 5.0));
        TemporalMonitoring<Double, Double> monitoring = new TemporalMonitoring<>(new DoubleDomain());
        monitoring.addProperty("test1", p -> (x -> x));
        monitoring.addProperty("test2", p -> (x -> x - 9));
        Function<Signal<Double>, Signal<Double>> m = monitoring.monitor(since, null);
        Signal<Double> result = m.apply(signal);
        assertEquals(signal.end(), result.end(), 0.0);
        assertEquals(5.0, result.start(), 0.0);
        SignalCursor<Double> c = result.getIterator(true);
        double time = 5.0;
        while (!c.completed()) {
            assertEquals(c.time() - 9.0, c.value(), 0.0, "Time: " + c.time());
            c.forward();
            time += 0.25;
        }
        assertEquals(10.25, time, 0.0);
    }

    @Test
    void testUnboundedSince() {
        Signal<Double> signal = TestUtils.createSignal(0.0, 10.0, 0.25, x -> x);
        Formula since = new SinceFormula(new AtomicFormula("test1"), new AtomicFormula("test2"));
        TemporalMonitoring<Double, Double> monitoring = new TemporalMonitoring<>(new DoubleDomain());
        monitoring.addProperty("test1", p -> (x -> x));
        monitoring.addProperty("test2", p -> (x -> x - 9));
        Function<Signal<Double>, Signal<Double>> m = monitoring.monitor(since, null);
        Signal<Double> result = m.apply(signal);
        assertEquals(signal.end(), result.end(), 0.0);
        assertEquals(0.0, result.start(), 0.0);
        SignalCursor<Double> c = result.getIterator(true);
        double time = 0.0;
        while (!c.completed()) {
            assertEquals(c.time() - 9.0, c.value(), 0.0, "Time: " + c.time());
            c.forward();
            time += 0.25;
        }
        assertEquals(10.25, time, 0.0);
    }


}
