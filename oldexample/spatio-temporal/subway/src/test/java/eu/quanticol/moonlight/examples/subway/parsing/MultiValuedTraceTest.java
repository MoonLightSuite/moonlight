package eu.quanticol.moonlight.examples.subway.parsing;

import eu.quanticol.moonlight.util.MultiValuedTrace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiValuedTraceTest {


    @Test
    void emptySignal() {
        MultiValuedTrace s = new MultiValuedTrace(0, 0);

        // We expect to have 0 dimensions
        assertEquals(0, s.dimensions());

        // We expect an exception when initializing an empty signal
        assertThrows(IllegalArgumentException.class, s::initialize);
    }

    @Test
    void wrongDimensions() {
        MultiValuedTrace s = new MultiValuedTrace(1, 1);
        MultiValuedTrace s1 = new MultiValuedTrace(2, 1);

        Integer[][] dim1 = new Integer[1][1];
        Integer[][] dim2 = new Integer[2][1];
        Integer[][] dim3 = new Integer[1][2];

        // First dimension is 1x1 space / time
        dim1[0][0] = 1;

        // Second dimension is 2x1 space / time
        dim2[0][0] = 1;
        dim2[1][0] = 2;

        // Third dimension is 1x2 space / time
        dim3[0][0] = 1;
        dim3[0][1] = 2;

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
        MultiValuedTrace s = new MultiValuedTrace(2, 1);

        Integer[][] dim1 = new Integer[2][1];
        Integer[][] dim2 = new Integer[2][1];

        // First dimension
        dim1[0][0] = 1;
        dim1[1][0] = 2;
        // Second dimension
        dim2[0][0] = 1;
        dim2[1][0] = 2;

        s.setDimension(dim1, 0).setDimension(dim2, 1).initialize();

        // We expect the resulting signal to have 2 dimensions
        assertEquals(2, s.dimensions());
    }
}