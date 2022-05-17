package eu.quanticol.moonlight.offline.algorithms.mfr;

import eu.quanticol.moonlight.offline.signal.mfr.MfrSignal;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static eu.quanticol.moonlight.offline.TestSignalUtils.basicSignal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MfrAlgorithmsTest {
    private static final int TOTAL_LOCATIONS = 5;
    private static final int[] locationsSet = new int[]{0, 2, 4};

    @Test
    void basicMapWorksCorrectly() {
        MfrSignal<Integer> signal = basicSignal(TOTAL_LOCATIONS, locationsSet);
        UnaryOperator<Integer> even = x -> x % 2;
        MfrAlgorithm<Integer> sp = new MfrAlgorithm<>(false);

        var result = sp.mapAlgorithm(even, signal);

        assertEquals(TOTAL_LOCATIONS, result.getNumberOfLocations());
        assertEquals(locationsSet, result.getLocationsSet());
        var firstValue = result.getSignalAtLocation(0).getValueAt(0.0);
        var secondValue = result.getSignalAtLocation(0).getValueAt(2.0);
        assertEquals(0, firstValue);
        assertEquals(1, secondValue);
    }

    @Test
    void basicFilterWorksCorrectly() {
        MfrSignal<Integer> signal = basicSignal(TOTAL_LOCATIONS, locationsSet);
        Predicate<Integer> even = x -> x % 2 == 0;
        MfrAlgorithm<Integer> sp = new MfrAlgorithm<>(false);

        var result = sp.filterAlgorithm(even, signal);

        assertEquals(TOTAL_LOCATIONS, result.getNumberOfLocations());
        assertEquals(locationsSet, result.getLocationsSet());
        var firstValue = result.getSignalAtLocation(0).getValueAt(0.0);
        var secondValue = result.getSignalAtLocation(0).getValueAt(2.0);
        assertEquals(2, firstValue);
        assertNull(secondValue);
    }
}