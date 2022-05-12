package eu.quanticol.moonlight.signal.online;

import eu.quanticol.moonlight.core.signal.Sample;
import eu.quanticol.moonlight.online.signal.TimeChain;
import eu.quanticol.moonlight.online.signal.TimeSegment;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimeChainTest {


    @Test
    void invalidEndingTest(){
        Sample<Double, Double> s = new TimeSegment<>(10.0, 1.0);

        assertThrows(IllegalArgumentException.class,
                     () -> new TimeChain<>(s, 5.0));
    }

    @Test
    void invalidEndingTest2(){
        List<Sample<Double, Double>> segments = new ArrayList<>();

        assertThrows(IllegalArgumentException.class,
                () -> new TimeChain<>(segments, 10.0));
    }

    @Test
    void invalidEndingTest3(){
        List<Sample<Double, Double>> segments = new ArrayList<>();
        segments.add(new TimeSegment<>(10.0, 1.0));
        segments.add(new TimeSegment<>(15.0, 1.0));

        assertThrows(IllegalArgumentException.class,
                () -> new TimeChain<>(segments, 10.0));
    }

    @Test
    void basicAPITest() {
        Sample<Double, Double> s = new TimeSegment<>(0.0, 1.0);
        TimeChain<Double, Double> chain = new TimeChain<>(s, 5.0);
        List<Sample<Double, Double>> expected = new ArrayList<>();
        expected.add(s);

        Sample<Double, Double> s2 = new TimeSegment<>(3.0, 2.0);
        expected.add(s2);
        chain.add(s2);

        assertEquals(expected, chain.toList());
    }

}