package eu.quanticol.moonlight.tests;


import eu.quanticol.moonlight.signal.ParallelSignalCursor;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.util.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author loreti
 */
class TestSpatialSignal {

    private static final double EPSILON = 0.0000001;


    @Test
    void testSignalInit() {
        int size = 100;
        SpatialTemporalSignal<Integer> s = new SpatialTemporalSignal<>(size);
        assertEquals(size, s.getNumberOfLocations());
        assertNotNull(s);
        assertTrue(Double.isNaN(s.start()));
        assertTrue(Double.isNaN(s.end()));
    }

    @Test
    void testSignalCretion() {
        SpatialTemporalSignal<Double> as = TestUtils.createSpatioTemporalSignal(100, 0.0, 0.1, 100.0, Math::pow);
        assertNotNull(as);
        assertEquals(0.0, as.start(), 0.0);
        assertEquals(100.0, as.end(), EPSILON);
    }

    @Test
    void testSignalCursor() {
        SpatialTemporalSignal<Double> as = TestUtils.createSpatioTemporalSignal(5, 0.0, 0.1, 100.0, Math::pow);
        ParallelSignalCursor<Double> cursor = as.getSignalCursor(true);
        assertEquals(0.0, as.start(), 0.0);
        assertEquals(100.0, as.end(), EPSILON);
        double time = 0.0;
        assertTrue(cursor.areSynchronized());
        assertEquals(0.0, cursor.getTime(), 0.0);
        while (time < 100.0) {
            assertEquals(time, cursor.getTime(), 0.0);
            time += 0.1;
            cursor.move(time);
        }
    }


}
