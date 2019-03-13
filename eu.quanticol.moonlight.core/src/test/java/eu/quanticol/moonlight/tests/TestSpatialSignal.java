/**
 * 
 */
package eu.quanticol.moonlight.tests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;

import eu.quanticol.moonlight.signal.ParallelSignalCursor;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;

/**
 * @author loreti
 *
 */
public class TestSpatialSignal {

	private static final double EPSILON = 0.0000001;


	@Test
	public void testSignalInit() {
		int size = 100;
		SpatioTemporalSignal<Integer> s = new SpatioTemporalSignal<>(size);
		assertEquals(size,s.getNumberOfLocations());
		assertNotNull(s);
		assertTrue(Double.isNaN(s.start()));
		assertTrue(Double.isNaN(s.end()));
	}
	
	@Test
	public void testSignalCretion() {
		SpatioTemporalSignal<Double> as = TestUtils.createSpatialSignal( 100 , 0.0, 0.1, 100.0, (t,i) -> Math.pow(t,i));
		assertNotNull(as);
		assertEquals(0.0,as.start(),0.0);
		assertEquals(100.0,as.end(),EPSILON);
	}

	@Test
	public void testSignalCursor() {
		SpatioTemporalSignal<Double> as = TestUtils.createSpatialSignal( 5 , 0.0, 0.1, 100.0, (t,i) -> Math.pow(t,i));
		ParallelSignalCursor<Double> cursor = as.getSignalCursor(true);
		assertEquals(0.0,as.start(),0.0);
		assertEquals(100.0,as.end(),EPSILON);
		double time = 0.0;
		assertTrue( cursor.areSynchronized() );
		assertEquals(0.0,cursor.getTime(),0.0);
		while (time<100.0) {
			assertEquals(time,cursor.getTime(),0.0);
			time += 0.1;
			cursor.move(time);
		}
	}


	

}
