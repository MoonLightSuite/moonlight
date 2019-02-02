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
    public void testOnce2() {
    	Signal<Double> signal = TestUtils.createSignal(0.0, 100.0, 0.1, x -> x);
    	Formula once = new OnceFormula(new AtomicFormula("test"), new Interval(0, 5.0));
    	TemporalMonitoring<Double,Double> monitoring = new TemporalMonitoring<>(new DoubleDomain());
    	monitoring.addProperty("test", p -> (x -> x));
    	Function<Signal<Double>,Signal<Double>> m = monitoring.monitor(once, null);
    	Signal<Double> result = m.apply(signal);
    	assertEquals( signal.end(), result.end(), 0.0);
    	assertEquals( 5.0 , result.start(), 0.0);
    	SignalCursor<Double> c = result.getIterator(true);
    	double time = 5.0;
    	while (!c.completed()) {
    		assertEquals(c.time(),c.value(),0.0000001);
    		c.forward();
    		time += 0.1;
    	}
    	assertTrue(time>100.0);
    }

    @Test
    public void testHistorically2() {
    	Signal<Double> signal = TestUtils.createSignal(0.0, 10.0, 0.25, x -> x);
    	Formula once = new HystoricallyFormula(new AtomicFormula("test"), new Interval(0, 5.0));
    	TemporalMonitoring<Double,Double> monitoring = new TemporalMonitoring<>(new DoubleDomain());
    	monitoring.addProperty("test", p -> (x -> x));
    	Function<Signal<Double>,Signal<Double>> m = monitoring.monitor(once, null);
    	Signal<Double> result = m.apply(signal);
    	assertEquals( signal.end(), result.end(), 0.0);
    	assertEquals( 5.0 , result.start(), 0.0);
    	SignalCursor<Double> c = result.getIterator(true);
    	double time = 5.0;
    	while (!c.completed()) {
    		assertEquals("Time: "+c.time(),c.time()-5.0,c.value(),0.0);
    		c.forward();
    		time += 0.25;
    	}
    	assertEquals(10.25,time,0.0);
    }    
    
}
