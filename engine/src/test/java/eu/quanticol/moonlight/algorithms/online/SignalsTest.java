package eu.quanticol.moonlight.algorithms.online;

import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.online.algorithms.Signals;
import eu.quanticol.moonlight.online.signal.SegmentInterface;
import eu.quanticol.moonlight.online.signal.TimeChain;
import eu.quanticol.moonlight.online.signal.TimeSegment;
import eu.quanticol.moonlight.online.signal.Update;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SignalsTest {

    @Test
    void singleRefineTest() {
        TimeChain<Double, Interval> chain = chainStub();

        Update<Double, Interval> u = new Update<>(0.0, 1.0, new Interval(1, 2));
        Signals.refine(chain, u, SignalsTest::refinesStub);

        assertEquals(new TimeSegment<>(0.0, new Interval(1, 2)), chain.get(0));
        assertEquals(new TimeSegment<>(1.0, Interval.any()), chain.get(1));
    }

    @Test
    void nonFromZeroRefineTest() {
        TimeChain<Double, Interval> chain = chainStub();

        Update<Double, Interval> u = new Update<>(1.0, 3.0, new Interval(1, 2));
        Signals.refine(chain, u, SignalsTest::refinesStub);

        assertEquals(new TimeSegment<>(0.0, Interval.any()), chain.get(0));
        assertEquals(new TimeSegment<>(1.0, new Interval(1, 2)), chain.get(1));
        assertEquals(new TimeSegment<>(3.0, Interval.any()), chain.get(2));
    }

    @Test
    void iterativeRefinement() {
        TimeChain<Double, Interval> chain = chainStub();

        Update<Double, Interval> u1 = new Update<>(1.0, 3.0, new Interval(-9, 9));
        Update<Double, Interval> u2 = new Update<>(1.0, 3.0, new Interval(1, 2));
        Update<Double, Interval> u3 = new Update<>(1.0, 3.0, new Interval(1, 1));
        Signals.refine(chain, u1, SignalsTest::refinesStub);

        assertEquals(new TimeSegment<>(0.0, Interval.any()), chain.get(0));
        assertEquals(new TimeSegment<>(1.0, new Interval(-9, 9)), chain.get(1));
        assertEquals(new TimeSegment<>(3.0, Interval.any()), chain.get(2));

        Signals.refine(chain, u2, SignalsTest::refinesStub);

        assertEquals(new TimeSegment<>(0.0, Interval.any()), chain.get(0));
        assertEquals(new TimeSegment<>(1.0, new Interval(1, 2)), chain.get(1));
        assertEquals(new TimeSegment<>(3.0, Interval.any()), chain.get(2));

        Signals.refine(chain, u3, SignalsTest::refinesStub);

        assertEquals(new TimeSegment<>(0.0, Interval.any()), chain.get(0));
        assertEquals(new TimeSegment<>(1.0, new Interval(1, 1)), chain.get(1));
        assertEquals(new TimeSegment<>(3.0, Interval.any()), chain.get(2));
    }

    @Test
    void iterativeBrokenRefinement() {
        TimeChain<Double, Interval> chain = chainStub();

        Update<Double, Interval> u1 = new Update<>(1.0, 3.0, new Interval(1, 2));
        Update<Double, Interval> u2 = new Update<>(1.0, 3.0, new Interval(1, 3));

        Signals.refine(chain, u1, SignalsTest::refinesStub);

        assertEquals(new TimeSegment<>(0.0, Interval.any()), chain.get(0));
        assertEquals(new TimeSegment<>(1.0, new Interval(1, 2)), chain.get(1));
        assertEquals(new TimeSegment<>(3.0, Interval.any()), chain.get(2));

        assertThrows(UnsupportedOperationException.class,
                     () -> Signals.refine(chain, u2, SignalsTest::refinesStub));
    }

    @Test
    void replacingUpdates() {
        TimeChain<Double, Interval> chain = chainStub();

        Update<Double, Interval> u1 = new Update<>(1.0, 3.0, new Interval(1, 2));
        Update<Double, Interval> u2 = new Update<>(3.0, 5.0, new Interval(1, 3));
        Update<Double, Interval> u3 = new Update<>(1.0, 5.0, new Interval(1, 1));

        Signals.refine(chain, u1, SignalsTest::refinesStub);
        Signals.refine(chain, u2, SignalsTest::refinesStub);
        Signals.refine(chain, u3, SignalsTest::refinesStub);

        assertEquals(new TimeSegment<>(0.0, Interval.any()), chain.get(0));
        assertEquals(new TimeSegment<>(1.0, new Interval(1, 1)), chain.get(1));
        assertEquals(new TimeSegment<>(5.0, Interval.any()), chain.get(2));
    }

    @Test
    void replacingUpdatesWithSameValueAsPreviousTime() {
        TimeChain<Double, Interval> chain = chainStub();

        Update<Double, Interval> u1 = new Update<>(1.0, 3.0, new Interval(1, 2));
        Update<Double, Interval> u2 = new Update<>(3.0, 5.0, new Interval(1, 3));
        Update<Double, Interval> u3 = new Update<>(2.0, 5.0, new Interval(1, 2));

        Signals.refine(chain, u1, SignalsTest::refinesStub);
        Signals.refine(chain, u2, SignalsTest::refinesStub);
        Signals.refine(chain, u3, SignalsTest::refinesStub);

        assertEquals(new TimeSegment<>(0.0, Interval.any()), chain.get(0));
        assertEquals(new TimeSegment<>(1.0, new Interval(1, 2)), chain.get(1));
        assertEquals(new TimeSegment<>(5.0, Interval.any()), chain.get(2));
    }

    @Test
    void replacingUpdatesWithSameValueAsNextTime() {
        TimeChain<Double, Interval> chain = chainStub();

        Update<Double, Interval> u1 = new Update<>(1.0, 3.0, new Interval(1, 5));
        Update<Double, Interval> u2 = new Update<>(3.0, 5.0, new Interval(1, 3));
        Update<Double, Interval> u3 = new Update<>(2.0, 3.0, new Interval(1, 3));

        Signals.refine(chain, u1, SignalsTest::refinesStub);
        Signals.refine(chain, u2, SignalsTest::refinesStub);
        Signals.refine(chain, u3, SignalsTest::refinesStub);

        assertEquals(new TimeSegment<>(0.0, Interval.any()), chain.get(0));
        assertEquals(new TimeSegment<>(1.0, new Interval(1, 5)), chain.get(1));
        assertEquals(new TimeSegment<>(2.0, new Interval(1, 3)), chain.get(2));
        assertEquals(new TimeSegment<>(5.0, Interval.any()), chain.get(3));
    }

    @Test
    void multipleOrderedConnectedRefineTest() {
        TimeChain<Double, Interval> chain = chainStub();

        Update<Double, Interval> u = new Update<>(1.0, 3.0, new Interval(1, 2));
        Signals.refine(chain, u, SignalsTest::refinesStub);

        u = new Update<>(3.0, 5.0, new Interval(2, 3));
        Signals.refine(chain, u, SignalsTest::refinesStub);

        u = new Update<>(5.0, 7.0, new Interval(3, 4));
        Signals.refine(chain, u, SignalsTest::refinesStub);

        assertEquals(new TimeSegment<>(0.0, Interval.any()), chain.get(0));
        assertEquals(new TimeSegment<>(1.0, new Interval(1, 2)), chain.get(1));
        assertEquals(new TimeSegment<>(3.0, new Interval(2, 3)), chain.get(2));
        assertEquals(new TimeSegment<>(5.0, new Interval(3, 4)), chain.get(3));
        assertEquals(new TimeSegment<>(7.0, Interval.any()), chain.get(4));
    }

    @Test
    void multipleShuffledConnectedRefineTest() {
        TimeChain<Double, Interval> chain = chainStub();

        Update<Double, Interval> u1 = new Update<>(1.0, 3.0, new Interval(1, 2));
        Update<Double, Interval> u2 = new Update<>(3.0, 5.0, new Interval(2, 3));
        Update<Double, Interval> u3 = new Update<>(5.0, 7.0, new Interval(3, 4));
        Signals.refine(chain, u2, SignalsTest::refinesStub);
        Signals.refine(chain, u3, SignalsTest::refinesStub);
        Signals.refine(chain, u1, SignalsTest::refinesStub);

        assertEquals(new TimeSegment<>(0.0, Interval.any()), chain.get(0));
        assertEquals(new TimeSegment<>(1.0, new Interval(1, 2)), chain.get(1));
        assertEquals(new TimeSegment<>(3.0, new Interval(2, 3)), chain.get(2));
        assertEquals(new TimeSegment<>(5.0, new Interval(3, 4)), chain.get(3));
        assertEquals(new TimeSegment<>(7.0, Interval.any()), chain.get(4));
    }

    @Test
    void chainedUpdates() {
        TimeChain<Double, Interval> chain = chainStub();

        TimeChain<Double, Interval> updates = generateUpdates();
        Signals.refineChain(chain, updates, SignalsTest::refinesStub);

        assertEquals(new TimeSegment<>(0.0, Interval.any()), chain.get(0));
        assertEquals(new TimeSegment<>(1.0, new Interval(1, 2)), chain.get(1));
        assertEquals(new TimeSegment<>(3.0, new Interval(2, 3)), chain.get(2));
        assertEquals(new TimeSegment<>(5.0, new Interval(3, 4)), chain.get(3));
        assertEquals(new TimeSegment<>(7.0, Interval.any()), chain.get(4));
    }

    // SELECTION TESTING

    @Test
    void totalSelection() {
        TimeChain<Double, Interval> chain = chainStub();
        double from = 3;
        double to = 5;
        TimeChain<Double, Interval> selected = Signals.select(chain, from, to);
        assertEquals(chain.toList(), selected.toList());
        assertEquals(5.0, selected.getEnd());
    }

    @Test
    void partialSelection() {
        TimeChain<Double, Interval> chain = chainStub();
        chain.add(new TimeSegment<>(1.0, new Interval(3, 4)));
        chain.add(new TimeSegment<>(3.0, new Interval(4, 5)));
        chain.add(new TimeSegment<>(7.0,Interval.any()));
        double from = 1.0;
        double to = 5.0;

        TimeChain<Double, Interval> selected = Signals.select(chain, from, to);
        TimeChain<Double, Interval> expected = chain.subChain(1, 3, to);

        assertEquals(expected.toList(), selected.toList());
        assertEquals(expected.getEnd(), selected.getEnd());
    }

    @Test
    void illegalSelection() {
        TimeChain<Double, Interval> chain = chainStub();
        double to = 3;
        double from = 5;
        assertThrows(UnsupportedOperationException.class,
                     () -> Signals.select(chain, from, to));
    }

    private static TimeChain<Double, Interval> generateUpdates() {
        SegmentInterface<Double, Interval> base = new TimeSegment<>(1.0, new Interval(1, 2));
        Double end = 7.0;
        TimeChain<Double, Interval> data = new TimeChain<>(base, end);

        data.add(new TimeSegment<>(3.0, new Interval(2, 3)));
        data.add(new TimeSegment<>(5.0, new Interval(3, 4)));

        return data;
    }

    private static TimeChain<Double, Interval> chainStub() {
        SegmentInterface<Double, Interval> s = new TimeSegment<>(0.0, Interval.any());

        return new TimeChain<>(s, Double.POSITIVE_INFINITY);
    }

    private static boolean refinesStub(Interval x, Interval y) {
        return x.contains(y);
    }
}