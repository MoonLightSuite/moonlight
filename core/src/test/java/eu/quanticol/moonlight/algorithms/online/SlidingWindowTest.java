package eu.quanticol.moonlight.algorithms.online;

import eu.quanticol.moonlight.domain.*;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.signal.online.SegmentInterface;
import eu.quanticol.moonlight.signal.online.TimeChain;
import eu.quanticol.moonlight.signal.online.TimeSegment;
import eu.quanticol.moonlight.signal.online.Update;
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

    @Disabled("WIP")
    @Test
    void test1() {
        TimeChain<Double, Double> arg = basicTimeChain();

        Update<Double, Double> u = new Update<>(0.0, 1.0, 2.0);

        List<Update<Double, Double>> results = globally(arg, u);
    }



    private TimeChain<Double, Double> basicTimeChain() {
        List<SegmentInterface<Double, Double>> segments = new ArrayList<>();
        segments.add(new TimeSegment<>(0.0, 3.0));
        segments.add(new TimeSegment<>(4.0, 5.0));

        double end = 5.0;

        return new TimeChain<>(segments, end);
    }


    @Disabled
    @Test
    void globTest1() {
        TimeChain<Double, AbstractInterval<Double>>
                input = new TimeChain<>(INF);
        add(input, 0, -2, INF);
        add(input, 4, -INF, INF);

        TimeChain<Double, AbstractInterval<Double>>
                output = new TimeChain<>(INF);
        add(output, 0, -INF, INF);

        List<Update<Double, AbstractInterval<Double>>>
                expected = toUpdates(output);

        List<Update<Double, AbstractInterval<Double>>> results = execGl(input);
        assertSame(expected, results);
    }

    @Disabled
    @Test
    void globTest2() {
        TimeChain<Double, AbstractInterval<Double>>
                input = new TimeChain<>(INF);
        add(input, 0, 1, INF);
        add(input, 4, -2, INF);
        add(input, 8, -INF, INF);

        TimeChain<Double, AbstractInterval<Double>>
                output = new TimeChain<>(INF);
        add(output, 0, -2, INF);
        add(output, 2, -INF, INF);

        List<Update<Double, AbstractInterval<Double>>>
                expected = toUpdates(output);

        List<Update<Double, AbstractInterval<Double>>> results = execGl(input);
        assertSame(expected, results);
    }

    @Disabled
    @Test
    void globTest3() {
        TimeChain<Double, AbstractInterval<Double>>
                input = new TimeChain<>(INF);
        add(input, 0, 1, INF);
        add(input, 4, -2, INF);
        add(input, 8, 1, INF);
        add(input, 13, -INF, INF);

        TimeChain<Double, AbstractInterval<Double>>
                output = new TimeChain<>(INF);
        add(output, 0, -2, INF);
        add(output, 2, -2, INF);
        add(output, 7, -INF, INF);

        List<Update<Double, AbstractInterval<Double>>>
                expected = toUpdates(output);

        List<Update<Double, AbstractInterval<Double>>> results = execGl(input);
        assertSame(expected, results);
    }

    @Disabled
    @Test
    void globTest4() {
        TimeChain<Double, AbstractInterval<Double>>
                input = new TimeChain<>(INF);
        add(input, 0, 1, 1);
        add(input, 4, -2, -2);
        add(input, 5, -2, INF);
        add(input, 8, 1, INF);
        add(input, 13, -1, INF);
        add(input, 19, -INF, INF);

        TimeChain<Double, AbstractInterval<Double>>
                output = new TimeChain<>(INF);
        add(output, 0, -2, -2);
        add(output, 2, -2, -2);
        add(output, 5, -2, INF);
        add(output, 7, -2, INF);
        add(output, 8, -1, INF);
        add(output, 13, -INF, INF);

        List<Update<Double, AbstractInterval<Double>>>
                expected = toUpdates(output);

        List<Update<Double, AbstractInterval<Double>>> results = execGl(input);
        assertSame(expected, results);
    }

    @Disabled
    @Test
    void globTest5() {
        TimeChain<Double, AbstractInterval<Double>>
                input = new TimeChain<>(INF);
        add(input, 0, 1, 1);
        add(input, 4, -2, -2);
        add(input, 5, 2, 2);
        add(input, 8, 2, INF);
        add(input, 12, 1, INF);
        add(input, 13, -1, INF);
        add(input, 22, -INF, INF);

        TimeChain<Double, AbstractInterval<Double>>
                output = new TimeChain<>(INF);
        add(output, 0, -2, -2);
        add(output, 2, -2, -2);
        add(output, 5, 2, 2);
        add(output, 6, 1, 2);
        add(output, 7, -1, 2);
        add(output, 8, -1, INF);
        add(output, 16, -INF, INF);

        List<Update<Double, AbstractInterval<Double>>>
                expected = toUpdates(output);

        List<Update<Double, AbstractInterval<Double>>> results = execGl(input);
        assertSame(expected, results);
    }

    @Disabled
    @Test
    void evTest1() {
        TimeChain<Double, AbstractInterval<Double>>
                input = new TimeChain<>(INF);
        add(input, 0, 1, 1);
        add(input, 4, -INF, INF);

        TimeChain<Double, AbstractInterval<Double>>
                output = new TimeChain<>(INF);
        add(output, 0, -INF, INF);

        List<Update<Double, AbstractInterval<Double>>>
                expected = toUpdates(output);

        List<Update<Double, AbstractInterval<Double>>> results = execEv(input);
        assertSame(expected, results);
    }

    @Disabled
    @Test
    void evTest2() {
        TimeChain<Double, AbstractInterval<Double>>
                input = new TimeChain<>(INF);
        add(input, 0, 1, 1);
        add(input, 4, 2, 2);
        add(input, 8, -INF, INF);

        TimeChain<Double, AbstractInterval<Double>>
                output = new TimeChain<>(INF);
        add(output, 0, -INF, INF);

        List<Update<Double, AbstractInterval<Double>>>
                expected = toUpdates(output);

        List<Update<Double, AbstractInterval<Double>>> results = execEv(input);
        assertSame(expected, results);
    }

    @Disabled
    @Test
    void evTest3() {
        TimeChain<Double, AbstractInterval<Double>>
                input = new TimeChain<>(INF);
        add(input, 0, 1, 1);
        add(input, 4, 2, 2);
        add(input, 8, -1, -1);
        add(input, 13, -INF, INF);

        TimeChain<Double, AbstractInterval<Double>>
                output = new TimeChain<>(INF);
        add(output, 0, -1, INF);
        add(output, 3, -INF, INF);

        List<Update<Double, AbstractInterval<Double>>>
                expected = toUpdates(output);

        List<Update<Double, AbstractInterval<Double>>> results = execEv(input);
        assertSame(expected, results);
    }

    @Disabled
    @Test
    void evTest4() {
        TimeChain<Double, AbstractInterval<Double>>
                input = new TimeChain<>(INF);
        add(input, 0, 1, 1);
        add(input, 4, 2, 2);
        add(input, 8, -1, -1);
        add(input, 13, -2, -2);
        add(input, 19, -INF, INF);

        TimeChain<Double, AbstractInterval<Double>>
                output = new TimeChain<>(INF);
        add(output, 0, -1, -1);
        add(output, 3, -2, -2);
        add(output, 5, -2, INF);
        add(output, 9, -INF, INF);

        List<Update<Double, AbstractInterval<Double>>>
                expected = toUpdates(output);

        List<Update<Double, AbstractInterval<Double>>> results = execEv(input);
        assertSame(expected, results);
    }

    @Disabled
    @Test
    void evTest5() {
        TimeChain<Double, AbstractInterval<Double>>
                input = new TimeChain<>(INF);
        add(input, 0, 1, 1);
        add(input, 4, 2, 2);
        add(input, 8, -1, -1);
        add(input, 13, -2, -2);
        add(input, 19, 2, 2);
        add(input, 22, -INF, INF);

        TimeChain<Double, AbstractInterval<Double>>
                output = new TimeChain<>(INF);
        add(output, 0, -1, -1);
        add(output, 3, -2, -2);
        add(output, 5, 2, 2);
        add(output, 8, 2, INF);
        add(output, 12, -INF, INF);

        List<Update<Double, AbstractInterval<Double>>>
                expected = toUpdates(output);

        List<Update<Double, AbstractInterval<Double>>> results = execEv(input);
        assertSame(expected, results);
    }

    @Disabled
    @Test
    void complexEv() {
        Interval opHorizon = new Interval(0.0, 1.0);
        Formula f = new EventuallyFormula(new NegationFormula(
                        new AtomicFormula("POSITIVE_X"))
                        ,  opHorizon)
                    ;
        List<Update<Double, AbstractInterval<Double>>> updates = new ArrayList<>();
        updates.add(new Update<>(0.0, 1.0, new AbstractInterval<>(2.0, 2.0)));
        updates.add(new Update<>(1.0, 2.0, new AbstractInterval<>(3.0, 3.0)));
        updates.add(new Update<>(2.0, 3.0, new AbstractInterval<>(4.0, 4.0)));
        updates.add(new Update<>(3.0, 4.0, new AbstractInterval<>(-1.0, -1.0)));
        updates.add(new Update<>(4.0, 5.0, new AbstractInterval<>(2.0, 2.0)));
        updates.add(new Update<>(5.0, 6.0, new AbstractInterval<>(2.0, 2.0)));
        updates.add(new Update<>(7.0, 8.0, new AbstractInterval<>(2.0, 2.0)));

        TimeChain<Double, AbstractInterval<Double>> input = new TimeChain<>(INF);
        input.add(new TimeSegment<>(0.0, new Interval(-2, -2)));
        input.add(new TimeSegment<>(1.0, new Interval(-3, -3)));

        SlidingWindow<AbstractInterval<Double>> w =
                new SlidingWindow<>(input, updates.get(0),
                                    opHorizon, domain::disjunction);

        w.run();

        w = new SlidingWindow<>(input, updates.get(1),
                                opHorizon, domain::disjunction);

        w.run();

    }

    private static <T> void assertSame(List<T> expected, List<T> toTest) {
        assertEquals(expected.size(), toTest.size());
        for(int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), toTest.get(i));
        }
    }

    private static List<Update<Double, AbstractInterval<Double>>>
    toUpdates(TimeChain<Double, AbstractInterval<Double>> chain)
    {
        //TODO handle end of updates
        List<Update<Double, AbstractInterval<Double>>> ups =
                                                            new ArrayList<>();

        for(SegmentInterface<Double, AbstractInterval<Double>> s: chain) {
            if(!ups.isEmpty()) {
                Update<Double, AbstractInterval<Double>> u = ups.remove(ups.size() - 1);
                ups.add(new Update<>(u.getStart(), s.getStart(), u.getValue()));
            }

            ups.add(new Update<>(s.getStart(), INF, s.getValue()));
        }
        return ups;
    }

    private static List<Update<Double, AbstractInterval<Double>>>
    execGl(TimeChain<Double, AbstractInterval<Double>> input)
    {
        Interval opHorizon = new Interval(0, 6);
        Update<Double, AbstractInterval<Double>> u =
                new Update<>(input.getFirst().getStart(), INF, null);

        SlidingWindow<AbstractInterval<Double>> w =
                new SlidingWindow<>(input, u, opHorizon, domain::conjunction);

        return w.run();
    }

    private static List<Update<Double, AbstractInterval<Double>>>
    execEv(TimeChain<Double, AbstractInterval<Double>> input)
    {
        Interval opHorizon = new Interval(10, 14);
        Update<Double, AbstractInterval<Double>> u =
                new Update<>(input.getFirst().getStart(), INF, null);

        SlidingWindow<AbstractInterval<Double>> w =
                new SlidingWindow<>(input, u, opHorizon, domain::disjunction);

        return w.run();
    }


    private static void add(TimeChain<Double, AbstractInterval<Double>> input,
                            double t, double a, double b)
    {
        input.add(new TimeSegment<>(t, new AbstractInterval<>(a, b)));
    }

    private static List<Update<Double, Double>> globally(
            TimeChain<Double, Double> arg,
            Update<Double, Double> u)
    {
        Interval h = new Interval(0, 6);
        return new SlidingWindow<>(arg, u, h, doubles::conjunction).run();
    }

}