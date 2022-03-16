package eu.quanticol.moonlight.online.algorithms;

import eu.quanticol.moonlight.core.base.Box;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.core.formula.Formula;
import eu.quanticol.moonlight.core.formula.Interval;
import eu.quanticol.moonlight.domain.*;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.formula.classic.NegationFormula;
import eu.quanticol.moonlight.formula.temporal.EventuallyFormula;
import eu.quanticol.moonlight.core.signal.Sample;
import eu.quanticol.moonlight.online.signal.TimeChain;
import eu.quanticol.moonlight.online.signal.TimeSegment;
import eu.quanticol.moonlight.online.signal.Update;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SlidingWindowTest {
    private static final Double INF = Double.POSITIVE_INFINITY;
    private static final AbsIntervalDomain<Double>
            domain = new AbsIntervalDomain<>(new DoubleDomain());

    private static final SignalDomain<Double> doubles = new DoubleDomain();


    @Test
    void basicTestChain() {
        TimeChain<Double, Double> arg = basicTimeChain();
        TimeChain<Double, Double> ups = simpleUpdateChain();

        TimeChain<Double, Double> results = globallyChain(arg, ups).get(0);

        System.out.println(results);
        assertEquals(new TimeSegment<>(0.0, 3.0), results.get(0));
        assertEquals(new TimeSegment<>(4.0, -1.0), results.get(1));
        assertEquals(10.0, results.getEnd());
    }

    @Test
    void basicTestUpdate() {
        TimeChain<Double, Double> arg = basicTimeChain();

        Update<Double, Double> u = new Update<>(0.0, 4.0, null);
        List<Update<Double, Double>> results = new ArrayList<>(globallyUpdate(arg, u));

        System.out.println(results);
        assertEquals(new Update<>(0.0, 4.0, 3.0), results.get(0));
        assertEquals(1, results.size());
    }


    private TimeChain<Double, Double> simpleUpdateChain() {
        List<Sample<Double, Double>> segments = new ArrayList<>();
        segments.add(new TimeSegment<>(4.0, 5.0));
        segments.add(new TimeSegment<>(7.0, 10.0));

        double end = 10.0;

        return new TimeChain<>(segments, end);
    }

    private TimeChain<Double, Double> basicTimeChain() {
        List<Sample<Double, Double>> segments = new ArrayList<>();
        segments.add(new TimeSegment<>(0.0, 3.0));
        segments.add(new TimeSegment<>(4.0, 5.0));
        segments.add(new TimeSegment<>(7.0, 10.0));
        segments.add(new TimeSegment<>(10.0, -1.0));

        double end = Double.POSITIVE_INFINITY;

        return new TimeChain<>(segments, end);
    }


    @Test
    void globTest1() {
        TimeChain<Double, Box<Double>>
                input = new TimeChain<>(INF);
        add(input, 0, -2, INF);
        add(input, 4, -INF, INF);

        TimeChain<Double, Box<Double>>
                output = new TimeChain<>(INF);
        add(output, 0, -INF, INF);

        List<Update<Double, Box<Double>>>
                expected = output.toUpdates();

        List<Update<Double, Box<Double>>> results = execGl(input);
        assertEquals(expected, results);
    }

    @Test
    void globTest2() {
        TimeChain<Double, Box<Double>>
                input = new TimeChain<>(INF);
        add(input, 0, 1, INF);
        add(input, 4, -2, INF);
        add(input, 8, -INF, INF);

        TimeChain<Double, Box<Double>>
                output = new TimeChain<>(INF);
        add(output, 0, -2, INF);
        add(output, 2, -INF, INF);

        List<Update<Double, Box<Double>>>
                expected = output.toUpdates();

        List<Update<Double, Box<Double>>> results = execGl(input);
        assertEquals(expected, results);
    }

    @Test
    void globTest3() {
        TimeChain<Double, Box<Double>>
                input = new TimeChain<>(INF);
        add(input, 0, 1, INF);
        add(input, 4, -2, INF);
        add(input, 8, 1, INF);
        add(input, 13, -INF, INF);

        TimeChain<Double, Box<Double>>
                output = new TimeChain<>(INF);
        add(output, 0, -2, INF);
        add(output, 7, -INF, INF);

        List<Update<Double, Box<Double>>>
                expected = output.toUpdates();

        List<Update<Double, Box<Double>>> results = execGl(input);
        assertEquals(expected, results);
    }

    @Test
    void globTest4() {
        TimeChain<Double, Box<Double>>
                input = new TimeChain<>(INF);
        add(input, 0, 1, 1);
        add(input, 4, -2, -2);
        add(input, 5, -2, INF);
        add(input, 8, 1, INF);
        add(input, 13, -1, INF);
        add(input, 19, -INF, INF);

        TimeChain<Double, Box<Double>>
                output = new TimeChain<>(INF);
        add(output, 0, -2, -2);
        add(output, 5, -2, INF);
        add(output, 8, -1, INF);
        add(output, 13, -INF, INF);

        List<Update<Double, Box<Double>>>
                expected = output.toUpdates();

        List<Update<Double, Box<Double>>> results = execGl(input);
        assertEquals(expected, results);
    }

    @Test
    void globTest5() {
        TimeChain<Double, Box<Double>>
                input = new TimeChain<>(INF);
        add(input, 0, 1, 1);
        add(input, 4, -2, -2);
        add(input, 5, 2, 2);
        add(input, 8, 2, INF);
        add(input, 12, 1, INF);
        add(input, 13, -1, INF);
        add(input, 22, -INF, INF);

        TimeChain<Double, Box<Double>>
                output = new TimeChain<>(INF);
        add(output, 0, -2, -2);
        add(output, 5, 2, 2);
        add(output, 6, 1, 2);
        add(output, 7, -1, 2);
        add(output, 8, -1, INF);
        add(output, 16, -INF, INF);

        List<Update<Double, Box<Double>>>
                expected = output.toUpdates();

        List<Update<Double, Box<Double>>> results = execGl(input);
        assertEquals(expected, results);
    }

    @Test
    void evTest1() {
        TimeChain<Double, Box<Double>>
                input = new TimeChain<>(INF);
        add(input, 0, 1, 1);
        add(input, 4, -INF, INF);

        TimeChain<Double, Box<Double>>
                output = new TimeChain<>(INF);
        add(output, 0, -INF, INF);

        List<Update<Double, Box<Double>>>
                expected = output.toUpdates();

        List<Update<Double, Box<Double>>> results = execEv(input);
        assertEquals(expected, results);
    }

    @Test
    void evTest2() {
        TimeChain<Double, Box<Double>>
                input = new TimeChain<>(INF);
        add(input, 0, 1, 1);
        add(input, 4, 2, 2);
        add(input, 8, -INF, INF);

        TimeChain<Double, Box<Double>>
                output = new TimeChain<>(INF);
        add(output, 0, -INF, INF);

        List<Update<Double, Box<Double>>>
                expected = output.toUpdates();

        List<Update<Double, Box<Double>>> results = execEv(input);
        assertEquals(expected, results);
    }

    @Test
    void evTest3() {
        TimeChain<Double, Box<Double>>
                input = new TimeChain<>(INF);
        add(input, 0, 1, 1);
        add(input, 4, 2, 2);
        add(input, 8, -1, -1);
        add(input, 13, -INF, INF);

        TimeChain<Double, Box<Double>>
                output = new TimeChain<>(INF);
        add(output, 0, -1, INF);
        add(output, 3, -INF, INF);

        List<Update<Double, Box<Double>>>
                expected = output.toUpdates();

        List<Update<Double, Box<Double>>> results = execEv(input);
        assertEquals(expected, results);
    }

    @Test
    void evTest4() {
        TimeChain<Double, Box<Double>>
                input = new TimeChain<>(INF);
        add(input, 0, 1, 1);
        add(input, 4, 2, 2);
        add(input, 8, -1, -1);
        add(input, 13, -2, -2);
        add(input, 19, -INF, INF);

        TimeChain<Double, Box<Double>>
                output = new TimeChain<>(INF);
        add(output, 0, -1, -1);
        add(output, 3, -2, -2);
        add(output, 5, -2, INF);
        add(output, 9, -INF, INF);

        List<Update<Double, Box<Double>>>
                expected = output.toUpdates();

        List<Update<Double, Box<Double>>> results = execEv(input);
        assertEquals(expected, results);
    }

    @Test
    void evTest5() {
        TimeChain<Double, Box<Double>>
                input = new TimeChain<>(INF);
        add(input, 0, 1, 1);
        add(input, 4, 2, 2);
        add(input, 8, -1, -1);
        add(input, 13, -2, -2);
        add(input, 19, 2, 2);
        add(input, 22, -INF, INF);

        TimeChain<Double, Box<Double>>
                output = new TimeChain<>(INF);
        add(output, 0, -1, -1);
        add(output, 3, -2, -2);
        add(output, 5, 2, 2);
        add(output, 8, 2, INF);
        add(output, 12, -INF, INF);

        List<Update<Double, Box<Double>>>
                expected = output.toUpdates();

        List<Update<Double, Box<Double>>> results = execEv(input);
        assertEquals(expected, results);
    }

    @Disabled("Should define some proper assertions")
    @Test
    void complexEv() {
        Interval opHorizon = new Interval(0.0, 1.0);
        Formula f = new EventuallyFormula(new NegationFormula(
                        new AtomicFormula("POSITIVE_X"))
                        ,  opHorizon)
                    ;
        List<Update<Double, Box<Double>>> updates = new ArrayList<>();
        updates.add(new Update<>(0.0, 1.0, new Box<>(2.0, 2.0)));
        updates.add(new Update<>(1.0, 2.0, new Box<>(3.0, 3.0)));
        updates.add(new Update<>(2.0, 3.0, new Box<>(4.0, 4.0)));
        updates.add(new Update<>(3.0, 4.0, new Box<>(-1.0, -1.0)));
        updates.add(new Update<>(4.0, 5.0, new Box<>(2.0, 2.0)));
        updates.add(new Update<>(5.0, 6.0, new Box<>(2.0, 2.0)));
        updates.add(new Update<>(7.0, 8.0, new Box<>(2.0, 2.0)));

        TimeChain<Double, Box<Double>> input = new TimeChain<>(INF);
        input.add(new TimeSegment<>(0.0, new Interval(-2, -2)));
        input.add(new TimeSegment<>(1.0, new Interval(-3, -3)));

        SlidingWindow<Box<Double>> w =
                new SlidingWindow<>(input, updates.get(0),
                                    opHorizon, domain::disjunction);

        w.run();

        w = new SlidingWindow<>(input, updates.get(1),
                                opHorizon, domain::disjunction);

        w.run();

    }

    private static List<Update<Double, Box<Double>>>
    execGl(TimeChain<Double, Box<Double>> input)
    {
        Interval opHorizon = new Interval(0, 6);
        Update<Double, Box<Double>> u =
                new Update<>(input.getFirst().getStart(), INF, null);

        SlidingWindow<Box<Double>> w =
                new SlidingWindow<>(input, u, opHorizon, domain::conjunction);

        return w.run();
    }

    private static List<Update<Double, Box<Double>>>
    execEv(TimeChain<Double, Box<Double>> input)
    {
        Interval opHorizon = new Interval(10, 14);
        Update<Double, Box<Double>> u =
                new Update<>(input.getFirst().getStart(), INF, null);

        SlidingWindow<Box<Double>> w =
                new SlidingWindow<>(input, u, opHorizon, domain::disjunction);

        return w.run();
    }


    private static void add(TimeChain<Double, Box<Double>> input,
                            double t, double a, double b)
    {
        input.add(new TimeSegment<>(t, new Box<>(a, b)));
    }

    private static List<TimeChain<Double, Double>> globallyChain(
            TimeChain<Double, Double> arg,
            TimeChain<Double, Double> ups)
    {
        Interval h = new Interval(0, 6);
        return new SlidingWindow<>(arg, ups, h, doubles::conjunction).runChain();
    }

    private static List<Update<Double, Double>> globallyUpdate(
            TimeChain<Double, Double> arg,
            Update<Double, Double> u)
    {
        Interval h = new Interval(0, 6);
        return new SlidingWindow<>(arg, u, h, doubles::conjunction).run();
    }

}