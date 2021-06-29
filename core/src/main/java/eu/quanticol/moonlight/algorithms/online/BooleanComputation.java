package eu.quanticol.moonlight.algorithms.online;

import eu.quanticol.moonlight.signal.online.*;

import java.io.Serializable;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 *
 */
public class BooleanComputation {

    private BooleanComputation() {}     // hidden constructor

    /**
     *
     * @param u update of the input signal
     * @param op  operation to be performed
     * @param <T> Time domain, usually expressed as a {@link Number}
     * @param <V> Input signal domain
     * @param <R> Output robustness domain
     * @return an update of the robustness signal in input
     */
    public static
    <T extends Comparable<T>, V, R>
    Update<T, R> atom(Update<T, V> u, Function<V, R> op)
    {
        return new Update<>(u.getStart(), u.getEnd(),
                                           op.apply(u.getValue()));
    }

    /**
     *
     * @param us update of the input signal
     * @param op  operation to be performed
     * @param <T> Time domain, usually expressed as a {@link Number}
     * @param <V> Input signal domain
     * @param <R> Output robustness domain
     * @return an update of the robustness signal in input
     */
    public static
    <T extends Comparable<T> & Serializable, V, R>
    TimeChain<T, R> atomSequence(TimeChain<T, V> us, Function<V, R> op)
    {
        List<SegmentInterface<T, R>> ls =
            us.stream()
              .map(s -> new TimeSegment<>(s.getStart(), op.apply(s.getValue())))
              .collect(Collectors.toList());

        return new TimeChain<>(ls, us.getEnd());
    }

    public static
    <T extends Comparable<T> & Serializable, V, R>
    TimeChain<T, R> atomSequence(Update<T, V> u, Function<V, R> op)
    {
        List<Update<T, R>> ups = new ArrayList<>();
        ups.add(atom(u, op));
        return Update.asTimeChain(ups);
    }

    /**
     *
     * @param u update of the operand
     * @param op  operation to be performed
     * @param <T> Time domain, usually expressed as a {@link Number}
     * @param <R> Output robustness domain
     * @return an update of the robustness signal in input
     */
    public static
    <T extends Comparable<T>, R>
    List<Update<T, R>> unary(Update<T, R> u, UnaryOperator<R> op)
    {
        Update<T, R> result = new Update<>(u.getStart(), u.getEnd(),
                op.apply(u.getValue()));
        List<Update<T, R>> results = new ArrayList<>();
        results.add(result);

        return results;
    }

    /**
     *
     * @param us chain of updates of the operand
     * @param op  operation to be performed
     * @param <T> Time domain, usually expressed as a {@link Number}
     * @param <R> Output robustness domain
     * @return an update of the robustness signal in input
     */
    public static
    <T extends Comparable<T> & Serializable, R>
    TimeChain<T, R> unarySequence(TimeChain<T, R> us, UnaryOperator<R> op)
    {
        List<SegmentInterface<T, R>> ls =
            us.stream()
              .map(s -> new TimeSegment<>(s.getStart(), op.apply(s.getValue())))
              .collect(Collectors.toList());


        return new TimeChain<>(ls, us.getEnd());
    }

    public static
    <T extends Comparable<T> & Serializable, R>
    TimeChain<T, R> binarySequence(TimeChain<T, R> c1,
                                   TimeChain<T, R> us,
                                   BinaryOperator<R> op)
    {
        List<SegmentInterface<T, R>> updates = new ArrayList<>(us.size());
        //TimeChain<T, R> c1 = s.select(us.getFirst().getStart(), us.getEnd());

        rightApplySequence(c1, updates, op, us);  // TODO: this should be different for
                                                  //       left and right operands

        return new TimeChain<>(updates, us.getEnd());

    }


    public static
    <T extends Comparable<T> & Serializable, R>
    List<Update<T, R>> binary(TimeChain<T, R> c1,
                              Update<T, R> u,
                              BinaryOperator<R> op)
    {
        List<Update<T, R>> updates;

        updates = rightApply(c1, op, u);  // TODO: this should be different for
                                           //       left and right operands

        return updates;
    }

    /**
     * Computes a list of updates to the robustness signal of a binary operator.
     *
     * 1 - if not overlapping, execute in parallel
     * 2 - if overlapping:
     *      2.1 - update the intersection with:
     *              U = (intersect.t_min, intersect.t_max, OP(u1,u2)
     *      2.2 - foreach segment as sss1 in s1.select:
     *              if sss1.start < u2.end:
     *                  try:
     *                      end = s1.select.peekNext();
     *                  ups.add(sss1.start, min(end, u2.end), OP(u1, sss1))
     *      2.2 - same for s2
     */
    /*
    public static
    <T extends Comparable<T> & Serializable, R extends Comparable<R>>
    List<Update<T, R>> binary(SignalInterface<T, R> s1,
                              SignalInterface<T, R> s2,
                              Update<T, R> u1, Update<T, R> u2,
                              BinaryOperator<R> op)
    {
        List<Update<T, R>> updates = new ArrayList<>();
        SegmentChain<T, R> p1 = s1.select(u2.getStart(), u2.getEnd());
        SegmentChain<T, R> p2 = s2.select(u1.getStart(), u1.getEnd());
        T tMin = max(u1.getStart(), u2.getStart());
        T tMax = min(u1.getEnd(), u2.getEnd());

        if(tMin.compareTo(tMax) > 0) {  // u1 and u2 are not overlapping
            parallelExec(p1, updates, op, u2);
            parallelExec(p2, updates, op, u1);
        } else {                        // u1 and u2 are overlapping
            updates.add(new Update<>(tMin, tMax,
                                     op.apply(u1.getValue(),u2.getValue())));

            // u1 starts before intersection so we must operate up to tMin
            if(u1.getStart().compareTo(tMin) < 0)
                overlappingBefore(p2, updates, op, u1, tMin);

            // ... u1 ends after the intersection
            if(u1.getEnd().compareTo(tMax) > 0)
                overlappingAfter(p2, updates, op, u1, tMax);

            // u2 starts before the intersection ...
            if(u2.getStart().compareTo(tMin) < 0)
                overlappingBefore(p1, updates, op, u2, tMin);

            // ... u2 end after the intersection
            if(u2.getEnd().compareTo(tMax) > 0)
                overlappingAfter(p1, updates, op, u2, tMax);

        }

        return updates;
    }

    public static
    <T extends Comparable<T> & Serializable, R extends Comparable<R>>
    List<Update<T, R>> binaryLeft(SignalInterface<T, R> s2,
                                  Update<T, R> u1,
                                  BinaryOperator<R> op)
    {
        List<Update<T, R>> updates = new ArrayList<>();
        SegmentChain<T, R> p2 = s2.select(u1.getStart(), u1.getEnd());
        T tMin = u1.getStart();
        T tMax = u1.getEnd();

        parallelExec(p2, updates, op, u1);

        return updates;
    }

    private static
    <T extends Comparable<T> & Serializable, R extends Comparable<R>>
    void overlappingBefore(SegmentChain<T, R> s,
                         List<Update<T, R>> updates,
                         BinaryOperator<R> op,
                         Update<T, R> u, T tMin)
    {
        ChainIterator<SegmentInterface<T, R>> itr = s.diffIterator();
        SegmentInterface<T, R> curr;

        while(itr.hasNext()) {
            curr = itr.next();
            if(curr.getStart().compareTo(tMin) < 0) {
                T nextStart = tryPeekNextStart(itr, s.getEnd());

                T end = min(nextStart, tMin);
                T start = max(curr.getStart(), u.getStart());
                Update<T, R> r = new Update<>(start, end,
                        op.apply(u.getValue(), curr.getValue()));
                updates.add(r);
            }
        }
    }

    private static
    <T extends Comparable<T> & Serializable, R extends Comparable<R>>
    void overlappingAfter(SegmentChain<T, R> s,
                          List<Update<T, R>> updates,
                          BinaryOperator<R> op,
                          Update<T, R> u, T tMax)
    {
        ChainIterator<SegmentInterface<T, R>> itr = s.diffIterator();
        SegmentInterface<T, R> curr;

        while(itr.hasNext()) {
            curr = itr.next();
            T nextStart = tryPeekNextStart(itr, s.getEnd());

            if(curr.getStart().compareTo(tMax) > 0 ||
               nextStart.compareTo(tMax) > 0)
            {
                T end = min(nextStart, u.getEnd());
                T start = max(curr.getStart(), tMax);
                Update<T, R> r = new Update<>(start, end,
                        op.apply(u.getValue(), curr.getValue()));
                updates.add(r);
            }
        }
    }*/

    private static
    <T extends Comparable<T> & Serializable, R>
    List<Update<T, R>> rightApply(TimeChain<T, R> s,
                    BinaryOperator<R> op, Update<T, R> u)
    {
        List<Update<T, R>> updates = new ArrayList<>();
        ChainIterator<SegmentInterface<T, R>> itr = s.chainIterator();
        SegmentInterface<T, R> curr;
        T nextTime;

        while(itr.hasNext()) {
            curr = itr.next();
            nextTime = tryPeekNextStart(itr, s.getEnd());
            exec(curr, u, nextTime, op, updates);
        }

        return updates;
    }

    private static <T extends Comparable<T> & Serializable, R>
    void exec(SegmentInterface<T, R> curr, Update<T, R> u, T nextTime,
              BinaryOperator<R> op, List<Update<T, R>> updates)
    {
        if(curr.getStart().compareTo(u.getEnd()) < 0
                && nextTime.compareTo(u.getStart()) >= 0)
        {
            T end = min(nextTime, u.getEnd());
            T start = max(curr.getStart(), u.getStart());
            if(!start.equals(end)) {
                Update<T, R> r = new Update<>(start, end,
                        op.apply(u.getValue(), curr.getValue()));
                updates.add(r);
            }
        }
    }

    private static
    <T extends Comparable<T> & Serializable, R>
    void rightApplySequence(TimeChain<T, R> s, List<SegmentInterface<T, R>> outputUps,
                            BinaryOperator<R> op, TimeChain<T, R> inputUps)
    {
        ChainIterator<SegmentInterface<T, R>> itr = s.chainIterator();
        ChainIterator<SegmentInterface<T, R>> utr = inputUps.chainIterator();
        SegmentInterface<T, R> curr;
        T nextTime;
        SegmentInterface<T, R> last = new TimeSegment<>(inputUps.getEnd(), null);
        SegmentInterface<T, R> fst = utr.next();
        SegmentInterface<T, R> snd = utr.tryPeekNext(last);

        while(itr.hasNext()) {
            curr = itr.next();
            nextTime = tryPeekNextStart(itr, s.getEnd());

            if(curr.getStart().compareTo(snd.getStart()) < 0
                    && nextTime.compareTo(fst.getStart()) >= 0)
            {
                T end = min(nextTime, snd.getStart());
                T start = max(curr.getStart(), fst.getStart());
                if(!start.equals(end)) {
                    SegmentInterface<T, R> r = new TimeSegment<>(start,
                                    op.apply(fst.getValue(), curr.getValue()));
                    outputUps.add(r);

                    if(utr.hasNext()) {
                        fst = utr.next();
                        snd = utr.tryPeekNext(last);
                    }
                }
            }
        }
    }

    private static <R extends Comparable<R>> R max(R a, R b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    private static <R extends Comparable<R>> R min(R a, R b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    /**
     * Fail-safe method for fetching data from next element (if exists).
     *
     * @param itr iterator to use for looking forward
     * @param defaultValue value to return in case of failure
     * @param <T> time domain of interest, usually a <code>Number</code>
     * @param <V> domain of the returned value
     * @return the next time value if present, otherwise the default one.
     */
    static <T extends Comparable<T>, V>
    T tryPeekNextStart(ChainIterator<SegmentInterface<T, V>> itr, T defaultValue)
    {
        if(itr.hasNext())
            return itr.peekNext().getStart();
        else
            return defaultValue;
    }
}
