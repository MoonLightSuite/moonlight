package eu.quanticol.moonlight.tests;

import eu.quanticol.moonlight.core.formula.Formula;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.formula.temporal.EventuallyFormula;
import eu.quanticol.moonlight.formula.temporal.GloballyFormula;
import eu.quanticol.moonlight.formula.temporal.UntilFormula;
import eu.quanticol.moonlight.offline.monitoring.TemporalMonitoring;
import eu.quanticol.moonlight.offline.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.offline.signal.Signal;
import eu.quanticol.moonlight.offline.signal.SignalCursor;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.core.formula.Interval;
import eu.quanticol.moonlight.util.Utils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestFormulae {


//    VariableArraySignal load(String name) throws IOException {
//        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
//        File file = new File(classLoader.getResource(name).getFile());
//        String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
//        return Deserializer.VARIABLE_ARRAY_SIGNAL.deserialize(contents);
//    }


    @Test
    void testEventually() {
        Signal<Double> signal = Utils.createSignal(0.0, 10.0, 0.1, x -> x);
        Formula eventually = new EventuallyFormula(new AtomicFormula("test"), new Interval(0, 5.0));
        TemporalMonitoring<Double, Double> monitoring = new TemporalMonitoring<>(new DoubleDomain());
        monitoring.addProperty("test", p -> (x -> x));
        TemporalMonitor<Double, Double> m = monitoring.monitor(eventually);
        Signal<Double> result = m.monitor(signal);
        assertEquals(signal.getEnd() - 5.0, result.getEnd(), 0.0);
        assertEquals(signal.getStart(), result.getStart(), 0.0);
        SignalCursor<Double, Double> c = result.getIterator(true);
        double time = 5.0;
        while (!c.isCompleted()) {
            assertEquals(c.getCurrentTime() + 5.0, c.getCurrentValue(), 0.0000001);
            c.forward();
            time += 0.1;
        }
        assertTrue(time > 10.0);
    }

    @Test
    void testAlways() {
        Signal<Double> signal = Utils.createSignal(0.0, 10.0, 0.25, x -> x);
        Formula globally = new GloballyFormula(new AtomicFormula("test"), new Interval(0, 5.0));
        TemporalMonitoring<Double, Double> monitoring = new TemporalMonitoring<>(new DoubleDomain());
        monitoring.addProperty("test", p -> (x -> x));
        TemporalMonitor<Double, Double> m = monitoring.monitor(globally);
        Signal<Double> result = m.monitor(signal);
        assertEquals(signal.getEnd() - 5.0, result.getEnd(), 0.0);
        assertEquals(signal.getStart(), result.getStart(), 0.0);
        SignalCursor<Double, Double> c = result.getIterator(true);
        double time = 5.0;
        while (!c.isCompleted()) {
            assertEquals(c.getCurrentTime(), c.getCurrentValue(), 0.0, "Time: " + c.getCurrentTime());
            c.forward();
            time += 0.25;
        }
        //assertEquals(10.25,time,0.0);
    }

    @Disabled("Based on old monitor")
    @Test
    void testUntil() {
        Signal<Double> signal = Utils.createSignal(0.0, 10.0, 0.25, x -> x);
        Formula until = new UntilFormula(new AtomicFormula("test1"), new AtomicFormula("test2"), new Interval(0, 5.0));
//        TemporalMonitoringOld<Double, Double> monitoring = new TemporalMonitoringOld<>(new DoubleDomain());
//        monitoring.addProperty("test1", p -> (x -> 1.0));
//        monitoring.addProperty("test2", p -> (x -> x - 9));
//        Function<Signal<Double>, Signal<Double>> m = monitoring.monitor(until, null);
        Function<Signal<Double>, Signal<Double>> m = null;
        Signal<Double> result = m.apply(signal);
        assertEquals(signal.getEnd(), result.getEnd(), 0.0);
        assertEquals(5.0, result.getStart(), 0.0);
        SignalCursor<Double, Double> c = result.getIterator(true);
        while (!c.isCompleted()) {
            assertEquals(c.getCurrentTime() - 9, c.getCurrentValue(), 0.0, "Time: " + c.getCurrentTime());
            c.forward();
        }
    }
    
    @Test
    void testUntilNew() {
        Signal<Double> signal = Utils.createSignal(0.0, 10.0, 0.25, x -> x);
        Formula until = new UntilFormula(new AtomicFormula("test1"), new AtomicFormula("test2"), new Interval(0, 5.0));
        TemporalMonitoring<Double, Double> monitoring = new TemporalMonitoring<>(new DoubleDomain());
        monitoring.addProperty("test1", p -> (x -> 1.0));
        monitoring.addProperty("test2", p -> (x -> x - 9));
        TemporalMonitor<Double, Double> m = monitoring.monitor(until);
        Signal<Double> result = m.monitor(signal);
        assertEquals(5.0, result.getEnd(), 0.0);
        assertEquals(0.0, result.getStart(), 0.0);
        SignalCursor<Double, Double> c = result.getIterator(true);
        double time = 5.0;
        while (!c.isCompleted()) {
            assertEquals(c.getCurrentTime() - 4, c.getCurrentValue(), 0.0, "Time: " + c.getCurrentTime());
            c.forward();
            time += 0.25;
        }
        assertEquals(10.25, time, 0.0);
    }

    @Test
    void testOnce() {
        Signal<Double> signal = Utils.createSignal(0.0, 10.0, 0.1, x -> x);
        Formula eventually = new EventuallyFormula(new AtomicFormula("test"), new Interval(0, 5.0));
        TemporalMonitoring<Double, Double> monitoring = new TemporalMonitoring<>(new DoubleDomain());
        monitoring.addProperty("test", p -> (x -> x));
        TemporalMonitor<Double, Double> m = monitoring.monitor(eventually);
        Signal<Double> result = m.monitor(signal);
        assertEquals(signal.getEnd() - 5.0, result.getEnd(), 0.0);
        assertEquals(signal.getStart(), result.getStart(), 0.0);
        SignalCursor<Double, Double> c = result.getIterator(true);
        double time = 5.0;
        while (!c.isCompleted()) {
            assertEquals(c.getCurrentTime() + 5.0, c.getCurrentValue(), 0.0000001);
            c.forward();
            time += 0.1;
        }
        assertTrue(time > 10.0);
    }
}