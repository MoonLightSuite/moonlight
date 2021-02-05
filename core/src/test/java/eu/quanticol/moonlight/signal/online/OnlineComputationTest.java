package eu.quanticol.moonlight.signal.online;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.IntervalDomain;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

class OnlineComputationTest {

    @Test
    void unary() {
    }

    @Test
    void binary() {
        OnlineSignal<Double> s = new OnlineSignal<>(Double.NEGATIVE_INFINITY,
                                                    Double.POSITIVE_INFINITY);
        OnlineSignal<Double> s1 = new OnlineSignal<>(Double.NEGATIVE_INFINITY,
                                                     Double.POSITIVE_INFINITY);

        OnlineSignal<Double> s2 = new OnlineSignal<>(Double.NEGATIVE_INFINITY,
                                                     Double.POSITIVE_INFINITY);

        AbstractInterval<Double> i1 = new AbstractInterval<>(4.0, 6.0);
        Update<Double, AbstractInterval<Double>> u1 = new Update<>(2.0, 3.0, i1);

        AbstractInterval<Double> i2 = new AbstractInterval<>(8.0, 8.0);
        Update<Double, AbstractInterval<Double>> u2 = new Update<>(2.0, 5.0, i2);


        List<Update<Double, AbstractInterval<Double>>> ups =
                OnlineComputation.binary(s1, s2, u1, u2, OnlineComputationTest::or);
        System.out.println(ups);
        s.refine(ups.get(0));
        s.refine(ups.get(1));

        //for(Update<Double, AbstractInterval<Double>> u : ups) {
        //    s.refine(u);
        //}
        System.out.println(s);
    }

    private static AbstractInterval<Double> or(AbstractInterval<Double> x,
                                               AbstractInterval<Double> y)
    {
        return new AbstractInterval<>(
                Math.max(x.getStart(), y.getStart()),
                Math.max(x.getEnd(), y.getEnd()));
    }
}