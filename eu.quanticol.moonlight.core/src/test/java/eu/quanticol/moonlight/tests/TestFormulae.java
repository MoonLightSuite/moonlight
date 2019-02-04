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

public class TestFormulae {
	
	
	private VariableArraySignal load( String name ) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource(name).getFile());
        String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
        VariableArraySignal signal = JSonSignalReader.readSignal(contents);
        return signal;
	}

    
    @Test
    public void testEventually() {
    	Signal<Double> signal = TestUtils.createSignal(0.0, 10.0, 0.1, x -> x);
    	Formula eventually = new EventuallyFormula(new AtomicFormula("test"), new Interval(0, 5.0));
    	TemporalMonitoring<Double,Double> monitoring = new TemporalMonitoring<>(new DoubleDomain());
    	monitoring.addProperty("test", p -> (x -> x));
    	Function<Signal<Double>,Signal<Double>> m = monitoring.monitor(eventually, null);
    	Signal<Double> result = m.apply(signal);
    	assertEquals( signal.end()-5.0, result.end(), 0.0);
    	assertEquals( signal.start(), result.start(), 0.0);
    	SignalCursor<Double> c = result.getIterator(true);
    	double time = 5.0;
    	while (!c.completed()) {
    		assertEquals(c.time()+5.0,c.value(),0.0000001);
    		c.forward();
    		time += 0.1;
    	}
    	assertTrue(time>10.0);
    }

    @Test
    public void testAlways() {
    	Signal<Double> signal = TestUtils.createSignal(0.0, 10.0, 0.25, x -> x);
    	Formula globally = new GloballyFormula(new AtomicFormula("test"), new Interval(0, 5.0));
    	TemporalMonitoring<Double,Double> monitoring = new TemporalMonitoring<>(new DoubleDomain());
    	monitoring.addProperty("test", p -> (x -> x));
    	Function<Signal<Double>,Signal<Double>> m = monitoring.monitor(globally, null);
    	Signal<Double> result = m.apply(signal);
		assertEquals( signal.end()-5.0, result.end(), 0.0);
		assertEquals( signal.start(), result.start(), 0.0);
    	SignalCursor<Double> c = result.getIterator(true);
    	double time = 5.0;
    	while (!c.completed()) {
    		assertEquals("Time: "+c.time(),c.time(),c.value(),0.0);
    		c.forward();
    		time += 0.25;
    	}
    	//assertEquals(10.25,time,0.0);
    }

	@Test
	public void testUntil() {
		Signal<Double> signal = TestUtils.createSignal(0.0, 10.0, 0.25, x -> x);
		Formula until = new UntilFormula(new AtomicFormula("test1"),new AtomicFormula("test2"),new Interval(0, 5.0));
		TemporalMonitoring<Double,Double> monitoring = new TemporalMonitoring<>(new DoubleDomain());
		monitoring.addProperty("test1", p -> (x -> 1.0));
		monitoring.addProperty("test2", p -> (x -> x - 9));
		Function<Signal<Double>,Signal<Double>> m = monitoring.monitor(until, null);
		Signal<Double> result = m.apply(signal);
		assertEquals( signal.end(), result.end(), 0.0);
		assertEquals( 5.0 , result.start(), 0.0);
		SignalCursor<Double> c = result.getIterator(true);
		double time = 5.0;
		while (!c.completed()) {
			assertEquals("Time: "+c.time(),c.time() - 9,c.value(),0.0);
			c.forward();
			time += 0.25;
		}
		assertEquals(10.25,time,0.0);
	}
    
}
