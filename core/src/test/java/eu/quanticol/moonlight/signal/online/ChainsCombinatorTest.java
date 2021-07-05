package eu.quanticol.moonlight.signal.online;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChainsCombinatorTest {

    @Test
    void testIllegalChain() {
        TimeChain<Integer, Integer> primary = primary();
        TimeChain<Integer, Integer> secondary = new TimeChain<>(0);

        assertThrows(IllegalArgumentException.class, () ->
           new ChainsCombinator<>(primary, secondary)
        );
    }

    @Test
    void testWithUpdateAddingElement() {
        ChainsCombinator<Integer, Integer> both =
                        new ChainsCombinator<>(primary(), secondary());
        List<SegmentInterface<Integer, Integer>> updates = new ArrayList<>();

        both.forEach((x, y) -> mockedOperation(y, updates));

        assertEquals(secondary().toList(), updates);
    }

    @Disabled("Not sure whether should be like this. See main class javadoc")
    @Test
    void testWithUpdateRemovingElement() {
        ChainsCombinator<Integer, Integer> both =
              new ChainsCombinator<>(primaryWithExtra(), secondaryWithoutMid());
        List<SegmentInterface<Integer, Integer>> updates = new ArrayList<>();

        both.forEach((x, y) -> mockedOperation(y, updates));

        assertEquals(secondaryWithoutMid().toList(), updates);
    }

    private SegmentInterface<Integer, Integer> mockedOperation(
            SegmentInterface<Integer, Integer> second,
            List<SegmentInterface<Integer, Integer>> list)
    {
        list.add(second);
        return second;
    }

    private TimeChain<Integer, Integer> primaryWithExtra() {
        List<SegmentInterface<Integer, Integer>> segments = new ArrayList<>();
        segments.add(new TimeSegment<>(1, 1));
        segments.add(new TimeSegment<>(2, 2));
        segments.add(new TimeSegment<>(3, 3));
        segments.add(new TimeSegment<>(4, 4));
        return new TimeChain<>(segments, 8);
    }

    private TimeChain<Integer, Integer> secondaryWithoutMid() {
        List<SegmentInterface<Integer, Integer>> segments = new ArrayList<>();
        segments.add(new TimeSegment<>(2, -2));
        return new TimeChain<>(segments, 4);
    }

    private TimeChain<Integer, Integer> primary() {
        List<SegmentInterface<Integer, Integer>> segments = new ArrayList<>();
        segments.add(new TimeSegment<>(1, 1));
        segments.add(new TimeSegment<>(2, 2));
        segments.add(new TimeSegment<>(4, 4));
        return new TimeChain<>(segments, 8);
    }

    private TimeChain<Integer, Integer> secondary() {
        List<SegmentInterface<Integer, Integer>> segments = new ArrayList<>();
        segments.add(new TimeSegment<>(2, -2));
        segments.add(new TimeSegment<>(3, -3));
        return new TimeChain<>(segments, 4);
    }
}