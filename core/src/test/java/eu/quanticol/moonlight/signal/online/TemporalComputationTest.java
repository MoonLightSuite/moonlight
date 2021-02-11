package eu.quanticol.moonlight.signal.online;

import eu.quanticol.moonlight.domain.AbstractInterval;
import org.junit.jupiter.api.Test;

import java.time.temporal.Temporal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemporalComputationTest {

    @Test
    void slidingWindow() {
        OnlineSignal<Double> s = init();
        SegmentChain<Double, AbstractInterval<Double>> chain = s.getSegments();

        AbstractInterval<Double> i1 = new AbstractInterval<>(3.0, 3.0);
        Update<Double, AbstractInterval<Double>> u = new Update<>(2.0, 4.0, i1);

        AbstractInterval<Double> opInterval = new AbstractInterval<>(3.0, 5.0);
        List<Update<Double, AbstractInterval<Double>>> r =
                TemporalComputation.slidingWindow(chain, u, opInterval,
                                                  TemporalComputationTest::max);

        // First update
        assertEquals(0, r.get(0).getStart());
        assertEquals(1, r.get(0).getEnd());
        assertEquals(new AbstractInterval<>(3.0, 3.0), r.get(0).getValue());
    }

    private static OnlineSignal<Double> init() {
        OnlineSignal<Double> s = new OnlineSignal<>(Double.NEGATIVE_INFINITY,
                                                    Double.POSITIVE_INFINITY);

        AbstractInterval<Double> i1 = new AbstractInterval<>(2.0, 2.0);
        Update<Double, AbstractInterval<Double>> u1 =
                new Update<>(0.0, 0.0, i1);
        s.refine(u1);

        AbstractInterval<Double> i2 = new AbstractInterval<>(1.0, 1.0);
        Update<Double, AbstractInterval<Double>> u2 =
                new Update<>(2.0, Double.POSITIVE_INFINITY, i2);
        s.refine(u2);

        return s;
    }

    private static AbstractInterval<Double> max(AbstractInterval<Double> x,
                                                AbstractInterval<Double> y)
    {
        return x.equals(y) ? x : (x.compareTo(y) > 0 ? x : y);
    }
}