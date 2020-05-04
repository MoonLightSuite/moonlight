package eu.quanticol.moonlight.examples.subway.parsing;

import eu.quanticol.moonlight.examples.subway.data.HashBiMap;
import eu.quanticol.moonlight.examples.subway.data.MultiValuedSignal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiValuedSignalTest {


    @Test
    void emptySignal() {
        MultiValuedSignal s = new MultiValuedSignal(0, 0);

        // We expect to have 0 dimensions
        assertEquals(0, s.dimensions());

        // We expect an exception when initializing an empty signal
        assertThrows(IllegalArgumentException.class, s::initialize);
    }

    @Test
    void wrongDimensions() {
        MultiValuedSignal s = new MultiValuedSignal(1, 1);
        MultiValuedSignal s1 = new MultiValuedSignal(2, 1);

        HashBiMap<Integer, Integer, Integer> dim1 = new HashBiMap<>();
        HashBiMap<Integer, Integer, Integer> dim2 = new HashBiMap<>();
        HashBiMap<Integer, Integer, Integer> dim3 = new HashBiMap<>();

        // First dimension is 1x1 space / time
        dim1.put(0, 0, 1);

        // Second dimension is 2x1 space / time
        dim2.put(0, 0, 1);
        dim2.put(1, 0, 2);

        // Third dimension is 1x2 space / time
        dim3.put(0, 0, 1);
        dim3.put(0, 1, 2);

        // We expect the second dimension to make the signal fail
        // because of space mismatch
        assertThrows(IllegalArgumentException.class,
                ()-> s.setDimension(dim1, 0).setDimension(dim2, 1));

        // We expect the third dimension to make the signal fail
        // because of time mismatch
        assertThrows(IllegalArgumentException.class,
                ()-> s.setDimension(dim1, 0).setDimension(dim3, 2));


        // We expect the third dimension to make the signal fail
        // because of right (space * time) but wrong space and wrong time
        assertThrows(IllegalArgumentException.class,
                ()-> s1.setDimension(dim2, 0).setDimension(dim3, 1));
    }

    @Test
    void simpleRightSignal() {
        MultiValuedSignal s = new MultiValuedSignal(2, 1);

        HashBiMap<Integer, Integer, Integer> dim1 = new HashBiMap<>();
        HashBiMap<Integer, Integer, Integer> dim2 = new HashBiMap<>();

        // First dimension
        dim1.put(0, 0, 1);
        dim1.put(1, 0, 2);
        // Second dimension
        dim2.put(0, 0, 1);
        dim2.put(1, 0, 2);

        s.setDimension(dim1, 0).setDimension(dim2, 1).initialize();

        // We expect the resulting signal to have 2 dimensions
        assertEquals(2, s.dimensions());
    }
}