package eu.quanticol.moonlight.signal.online;

import java.io.Serializable;
import java.util.function.BinaryOperator;

/**
 * Given a primary and a secondary chain, it mutates the primary chain 
 * by combining the respective values in the given order
 * 
 * <pre>
 *   Primary    Secondary       Result (Updated Primary)
 *     1                          1 
 *     |                          |
 *     2            2             2
 *     |            |             |
 *     4            3             3
 *     |            |             |
 *     .            4             4
 *     .                          .
 *     .                          .
 *                                .
 * </pre>
 * 
 * 
 * @param <T> Type on which the order relation is defined
 * @param <V> Type of which the values will be combined in the resulting chain
 */
public class ChainsCombinator
        <T extends Comparable<T> & Serializable, V>
{
    private final ChainIterator<SegmentInterface<T, V>> primary;
    private final ChainIterator<SegmentInterface<T, V>> secondary;
    private final SegmentInterface<T, V> primaryEnd;
    private final SegmentInterface<T, V> secondaryEnd;

    SegmentInterface<T, V> primaryCurr;
    SegmentInterface<T, V> primaryNext;
    SegmentInterface<T, V> secondaryCurr;
    SegmentInterface<T, V> secondaryNext;

    public ChainsCombinator(TimeChain<T, V> primaryChain,
                            TimeChain<T, V> secondaryChain)
    {
        if(primaryChain.isEmpty() || secondaryChain.isEmpty())
            throw new IllegalArgumentException("Both chains must not be empty");

        primary = primaryChain.chainIterator();
        secondary = secondaryChain.chainIterator();
        primaryEnd = new TimeSegment<>(primaryChain.getEnd(), null);
        secondaryEnd = new TimeSegment<>(secondaryChain.getEnd(), null);
    }

    public void forEach(BinaryOperator<SegmentInterface<T, V>> operation)
    {
        movePrimary();
        moveSecondary();
        do {
            if (notYetRelevant()) {
                movePrimary();
                continue;
            }

            if (notAnymoreRelevant()) {
                moveSecondary();
            }

            process(operation);
        } while(stillToProcess());
    }

    private void process(BinaryOperator<SegmentInterface<T, V>> op)
    {
        op.apply(primaryCurr, secondaryCurr);
        moveSecondary();
    }

    private void movePrimary() {
        primaryCurr = primary.hasNext() ? primary.next() : primaryEnd;
        primaryNext = primary.tryPeekNext(primaryEnd);
    }

    private void moveSecondary() {
        secondaryCurr = secondary.hasNext() ? secondary.next() : secondaryEnd;
        secondaryNext = secondary.tryPeekNext(secondaryEnd);
    }

    private boolean stillToProcess() {
        return secondaryEnd.compareTo(primaryCurr) > 0;
    }

    private boolean notAnymoreRelevant()
    {
        return primaryCurr.compareTo(secondaryNext) > 0;
    }

    private boolean notYetRelevant()
    {
        return primaryNext.compareTo(secondaryCurr) <= 0;
    }
}
