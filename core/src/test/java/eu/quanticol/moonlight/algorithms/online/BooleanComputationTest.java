package eu.quanticol.moonlight.algorithms.online;

import eu.quanticol.moonlight.domain.*;
import eu.quanticol.moonlight.signal.online.*;

import static eu.quanticol.moonlight.algorithms.online.BooleanComputation.*;

import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;

import static org.junit.jupiter.api.Assertions.*;


class BooleanComputationTest {


    @Test
    void testSpecial() {
        List<Update<Double, AbstractInterval<Double>>> ups1 = updatesFirst();

        List<Update<Double, AbstractInterval<Double>>> r = new ArrayList<>();
        for(Update<Double, AbstractInterval<Double>> u: ups1) {
            r.addAll(binary(dataSecond(), u, intervalOr()));
        }
        TimeChain<Double, AbstractInterval<Double>> r1 = Update.asTimeChain(r);

        TimeChain<Double, AbstractInterval<Double>> r2 =
           binarySequence(dataSecond(), Update.asTimeChain(ups1), intervalOr());

        assertEquals(r1, r2);

    }

    private static List<Update<Double, AbstractInterval<Double>>> updatesFirst()
    {
        List<Update<Double, AbstractInterval<Double>>> updates =
                new ArrayList<>();
        updates.add(new Update<>(0.0, 3.0, new AbstractInterval<>(-1.0, -1.0)));
        updates.add(new Update<>(3.0, 5.0, new AbstractInterval<>(-2.0, -2.0)));
        updates.add(new Update<>(5.0, 9.0, new AbstractInterval<>(-2.0,
                Double.POSITIVE_INFINITY)));

        return updates;
    }

    private static TimeChain<Double, AbstractInterval<Double>> dataSecond() {
        TimeChain<Double, AbstractInterval<Double>> data = new TimeChain<>(
                Double.POSITIVE_INFINITY);
        data.add(new TimeSegment<>(0.0, new AbstractInterval<>(1.0, 1.0)));
        data.add(new TimeSegment<>(4.0, new AbstractInterval<>(-2.0, -2.0)));
        data.add(new TimeSegment<>(8.0, new AbstractInterval<>(1.0, 1.0)));
        data.add(new TimeSegment<>(13.0, new AbstractInterval<>(-1.0, -1.0)));
        data.add(new TimeSegment<>(19.0,
                new AbstractInterval<>(Double.NEGATIVE_INFINITY,
                        Double.POSITIVE_INFINITY)));

        return data;
    }

    private static BinaryOperator<AbstractInterval<Double>> intervalOr() {
        return (x, y) -> new AbsIntervalDomain<>(new DoubleDomain())
                .disjunction(x, y);
    }


    @Test
    void atomTest() {
        Update<Integer, Integer> u = basicUpdate();
        Update<Integer, Integer> r = atom(u, BooleanComputationTest::positive);
        assertEquals(u, r);
    }

    @Test
    void atomSequenceTest() {
        TimeChain<Integer, Integer> ups = basicUpdateChain();

        TimeChain<Integer, Integer> r = atomSequence(ups,
                                            BooleanComputationTest::positive);

        assertEquals(ups, r);
    }

    @Test
    void unaryTest() {
        Update<Integer, Integer> u = basicUpdate();

        Update<Integer, Integer> r = unary(u, BooleanComputationTest::not)
                                                                        .get(0);

        assertEquals(u.getStart(), r.getStart());
        assertEquals(u.getEnd(), r.getEnd());
        assertEquals(- u.getValue(), r.getValue());
    }

    @Test
    void unarySequenceTest() {
        TimeChain<Integer, Integer> ups = basicUpdateChain();

        TimeChain<Integer, Integer> r = BooleanComputation
                .unarySequence(ups, BooleanComputationTest::not);

        // First update
        assertEquals(ups.get(0).getStart(), r.get(0).getStart());
        assertEquals(ups.get(1).getStart(), r.get(1).getStart());
        assertEquals(- ups.get(0).getValue(), r.get(0).getValue());
        assertEquals(- ups.get(1).getValue(), r.get(1).getValue());
        assertEquals(ups.getEnd(), r.getEnd());
        assertEquals(ups.size(), r.size());
    }

    @Test
    void binaryTest() {
        Update<Integer, Integer> u = basicUpdate();
        TimeChain<Integer, Integer> chain = basicUpdateChain(); //TODO: should be used basicSignalChain instead

        List<Update<Integer, Integer>> r = binary(chain, u,
                                                  BooleanComputationTest::and);

        assertEquals(u.getStart(), r.get(0).getStart());
        assertEquals(u.getEnd(), r.get(0).getEnd());
        assertEquals(chain.get(1).getValue(), r.get(0).getValue());
        assertEquals(1, r.size());
    }

    @Test
    void binarySequenceTest() {
        TimeChain<Integer, Integer> ups = basicUpdateChain();
        TimeChain<Integer, Integer> chain = basicSignalChain();

        TimeChain<Integer, Integer> r = binarySequence(chain, ups,
                                                BooleanComputationTest::and);

        assertEquals(ups.get(0).getStart(), r.get(0).getStart());
        assertEquals(chain.get(1).getStart(), r.get(1).getStart());
        assertEquals(ups.getEnd(), r.getEnd());

        assertEquals(chain.get(0).getValue(), r.get(0).getValue());
        assertEquals(ups.get(1).getValue(), r.get(1).getValue());
        assertEquals(2, r.size());
    }

    private Update<Integer, Integer> basicUpdate() {
        return new Update<>(2, 3, 8);
    }

    private TimeChain<Integer, Integer> basicUpdateChain() {
        List<SegmentInterface<Integer, Integer>> values = new ArrayList<>();
        values.add(new TimeSegment<>(0, 3));
        values.add(new TimeSegment<>(1, 4));
        return new TimeChain<>(values, 5);
    }

    private TimeChain<Integer, Integer> basicSignalChain() {
        List<SegmentInterface<Integer, Integer>> values = new ArrayList<>();
        values.add(new TimeSegment<>(0, -1));
        values.add(new TimeSegment<>(4, 5));
        return new TimeChain<>(values, Integer.MAX_VALUE);
    }

    private static int and(int x, int y) {
        return Math.min(x, y);
    }

    private static int not(int x) {
        return -x;
    }

    private static int positive(int x) {
        return x;
    }
}