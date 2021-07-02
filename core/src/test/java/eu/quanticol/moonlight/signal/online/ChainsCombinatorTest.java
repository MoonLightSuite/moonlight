package eu.quanticol.moonlight.signal.online;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChainsCombinatorTest {
    @Test
    void firstTest() {
        ChainsCombinator<Integer, Integer> both =
                        new ChainsCombinator<>(primary(), secondary());
        List<SegmentInterface<Integer, Integer>> updates = new ArrayList<>();

        both.forEach((x, y) -> mockedOperation(y, updates));

        assertEquals(secondary().toList(), updates);
    }

    private SegmentInterface<Integer, Integer> mockedOperation(
            SegmentInterface<Integer, Integer> second,
            List<SegmentInterface<Integer, Integer>> list)
    {
        list.add(second);
        return second;
    }

    private TimeChain<Integer, Integer> primary() {
        List<SegmentInterface<Integer, Integer>> segments = new ArrayList<>();
        segments.add(new TimeSegment<>(1, 1));
        segments.add(new TimeSegment<>(2, 2));
        segments.add(new TimeSegment<>(4, 4));
        segments.add(new TimeSegment<>(5, 5));
        return new TimeChain<>(segments, 8);
    }

    private TimeChain<Integer, Integer> secondary() {
        List<SegmentInterface<Integer, Integer>> segments = new ArrayList<>();
        segments.add(new TimeSegment<>(2, -2));
        segments.add(new TimeSegment<>(3, -3));
        return new TimeChain<>(segments, 4);
    }
}