package eu.quanticol.moonlight.algorithms.online;

import eu.quanticol.moonlight.domain.*;
import eu.quanticol.moonlight.signal.online.OnlineSignal;
import eu.quanticol.moonlight.signal.online.TimeChain;
import eu.quanticol.moonlight.signal.online.Update;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemporalComputationTest {

    @Test
    void slidingWindow() {
        AbsIntervalDomain<Double> domain = new AbsIntervalDomain<>(new DoubleDomain());

        OnlineSignal<Double> s = init();
        TimeChain<Double, AbstractInterval<Double>> chain = s.getSegments();
        AbstractInterval<Double> i1 = new AbstractInterval<>(3.0, 3.0);
        Update<Double, AbstractInterval<Double>> u = new Update<>(2.0, 4.0, i1);
        s.refine(u);

        Interval opInterval = new Interval(3.0, 5);
        List<Update<Double, AbstractInterval<Double>>> r =
                TemporalComputation.slidingWindow(chain, u, opInterval,
                                                  domain::disjunction);

        // First update
        assertEquals(0, r.get(0).getStart());
        assertEquals(1, r.get(0).getEnd());
        assertEquals(new AbstractInterval<>(3.0, Double.POSITIVE_INFINITY), r.get(0).getValue());
    }

    private static OnlineSignal<Double> init() {
        SignalDomain<Double> domain = new DoubleDomain();
        OnlineSignal<Double> s = new OnlineSignal<>(domain);

        AbstractInterval<Double> i1 = new AbstractInterval<>(2.0, 2.0);
        Update<Double, AbstractInterval<Double>> u1 =
                new Update<>(0.0, 0.0, i1);
        s.refine(u1);

        AbstractInterval<Double> i2 = new AbstractInterval<>(1.0, Double.POSITIVE_INFINITY);
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