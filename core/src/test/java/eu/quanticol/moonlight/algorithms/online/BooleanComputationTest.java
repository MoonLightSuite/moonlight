package eu.quanticol.moonlight.signal.online;

import eu.quanticol.moonlight.algorithms.online.BooleanComputation;
import eu.quanticol.moonlight.domain.AbstractInterval;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BooleanComputationTest {

    @Test
    void unary() {
        AbstractInterval<Double> i1 = new AbstractInterval<>(4.0, 6.0);
        Update<Double, AbstractInterval<Double>> u = new Update<>(2.0, 3.0, i1);

        Update<Double, AbstractInterval<Double>> r =
                    BooleanComputation.unary(u, BooleanComputationTest::not);

        // First update
        assertEquals(2, r.getStart());
        assertEquals(3, r.getEnd());
        assertEquals(new AbstractInterval<>(-6.0, -4.0), r.getValue());
    }

    @Test
    void binary() {
        OnlineSignal<Double> s1 = new OnlineSignal<>(Double.NEGATIVE_INFINITY,
                                                     Double.POSITIVE_INFINITY);

        OnlineSignal<Double> s2 = new OnlineSignal<>(Double.NEGATIVE_INFINITY,
                                                     Double.POSITIVE_INFINITY);

        AbstractInterval<Double> i1 = new AbstractInterval<>(4.0, 6.0);
        Update<Double, AbstractInterval<Double>> u1 =
                                        new Update<>(2.0, 3.0, i1);

        AbstractInterval<Double> i2 = new AbstractInterval<>(8.0, 8.0);
        Update<Double, AbstractInterval<Double>> u2 =
                                        new Update<>(2.0, 5.0, i2);


        List<Update<Double, AbstractInterval<Double>>> ups =
                BooleanComputation.binary(s1, s2, u1, u2,
                                          BooleanComputationTest::or);

        assertEquals(2, ups.size());

        // First update
        assertEquals(2, ups.get(0).getStart());
        assertEquals(3, ups.get(0).getEnd());
        assertEquals(new AbstractInterval<>(8.0, 8.0), ups.get(0).getValue());

        // Second update
        assertEquals(3, ups.get(1).getStart());
        assertEquals(5, ups.get(1).getEnd());
        assertEquals(new AbstractInterval<>(8.0, Double.POSITIVE_INFINITY),
                     ups.get(1).getValue());
    }

    private static AbstractInterval<Double> or(AbstractInterval<Double> x,
                                               AbstractInterval<Double> y)
    {
        return new AbstractInterval<>(
                Math.max(x.getStart(), y.getStart()),
                Math.max(x.getEnd(), y.getEnd()));
    }

    private static AbstractInterval<Double> not(AbstractInterval<Double> x)
    {
        return new AbstractInterval<>(-x.getEnd(), -x.getStart());
    }
}