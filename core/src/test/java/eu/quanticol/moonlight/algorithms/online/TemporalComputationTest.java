package eu.quanticol.moonlight.algorithms.online;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.signal.online.OnlineSignal;
import eu.quanticol.moonlight.signal.online.SegmentChain;
import eu.quanticol.moonlight.signal.online.Update;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemporalComputationTest {

    @Test
    void slidingWindow() {
        OnlineSignal<Double> s = init();
        SegmentChain<Double, AbstractInterval<Double>> chain = s.getSegments();

        AbstractInterval<Double> i1 = new AbstractInterval<>(3.0, 3.0);
        Update<Double, AbstractInterval<Double>> u = new Update<>(2.0, 4.0, i1);

        Interval opInterval = new Interval(3.0, 5.0);
        List<Update<Double, AbstractInterval<Double>>> r =
                TemporalComputation.slidingWindow(chain, u, opInterval,
                                                  TemporalComputationTest::max);

        // First update
        assertEquals(0, r.get(0).getStart());
        assertEquals(1, r.get(0).getEnd());
        assertEquals(new AbstractInterval<>(3.0, 3.0), r.get(0).getValue());
    }

    private static OnlineSignal<Double> init() {
        SignalDomain<Double> domain = new DoubleDomain();
        OnlineSignal<Double> s = new OnlineSignal<>(domain);

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