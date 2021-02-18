package eu.quanticol.moonlight.signal.online;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 */
public class BooleanComputation {

    private BooleanComputation() {}     // hidden constructor

    /**
     *
     * @param u update of the operand
     * @param op  operation to be performed
     * @param <T> Time domain, usually expressed in Numbers
     * @param <V> Input signal domain
     * @param <R> Output robustness domain
     * @return an update of the robustness signal in input
     */
    public static
    <T extends Comparable<T>, V extends Comparable<V>, R extends Comparable<R>>
    Update<T, R> unary(Update<T, V> u, Function<V, R> op)
    {
        return new Update<>(u.getStart(), u.getEnd(), op.apply(u.getValue()));
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
     *
     * @param s1 robustness signal of the first operand
     * @param s2 robustness signal of the second operand
     * @param u1 update of the first operand
     * @param u2 update of the second operand
     * @param op operation to be performed
     * @param <T> Time domain, usually expressed in Numbers
     * @param <V> Input signal domain
     * @param <R> Output robustness domain
     * @return a list of updates for the robustness signal in input
     */
    public static
    <T extends Comparable<T> & Serializable, V extends Comparable<V>, R extends Comparable<R>>
    List<Update<T, R>> binary(SignalInterface<T, V> s1, SignalInterface<T, V> s2,
                              Update<T, V> u1, Update<T, V> u2,
                              BiFunction<V, V, R> op)
    {
        List<Update<T, R>> updates = new ArrayList<>();
        SegmentChain<T, V> p1 = s1.select(u2.getStart(), u2.getEnd());
        SegmentChain<T, V> p2 = s2.select(u1.getStart(), u1.getEnd());
        T tMin = max(u1.getStart(), u2.getStart());
        T tMax = min(u1.getEnd(), u2.getEnd());

        if(tMin.compareTo(tMax) > 0) {  // u1 and u2 are not overlapping
            parallelExec(p1, updates, op, u2);
            parallelExec(p2, updates, op, u1);
        } else {    // u1 and u2 are overlapping
            updates.add(new Update<>(tMin, tMax,
                                     op.apply(u1.getValue(),u2.getValue())));

            // u1 starts before intersection so we must operate up to tMin
            if(u1.getStart().compareTo(tMin) < 0)
                overlappingBefore(p2, updates, op, u1, tMin);

            // u1 ends after the intersection
            if(u1.getEnd().compareTo(tMax) > 0)
                overlappingAfter(p2, updates, op, u1, tMax);

            // u2 starts before intersection ...
            if(u2.getStart().compareTo(tMin) < 0)
                overlappingBefore(p1, updates, op, u2, tMin);

            if(u2.getEnd().compareTo(tMax) > 0)
                overlappingAfter(p1, updates, op, u2, tMax);

            
        }

        return updates;
    }

    private static
    <T extends Comparable<T> & Serializable,
     V extends Comparable<V>,
     R extends Comparable<R>>
    void overlappingBefore(SegmentChain<T, V> s,
                         List<Update<T, R>> updates,
                         BiFunction<V, V, R> op,
                         Update<T, V> u, T tMin)
    {
        DiffIterator<SegmentInterface<T, V>> itr = s.diffIterator();
        SegmentInterface<T, V> curr;

        while(itr.hasNext()) {
            curr = itr.next();
            if(curr.getStart().compareTo(tMin) < 0) {
                T nextStart = tryPeekNext(itr, s.getEnd());

                T end = min(nextStart, tMin);
                T start = max(curr.getStart(), u.getStart());
                Update<T, R> r = new Update<>(start, end,
                        op.apply(u.getValue(), curr.getValue()));
                updates.add(r);
            }
        }
    }

    private static
    <T extends Comparable<T> & Serializable,
            V extends Comparable<V>,
            R extends Comparable<R>>
    void overlappingAfter(SegmentChain<T, V> s,
                           List<Update<T, R>> updates,
                           BiFunction<V, V, R> op,
                           Update<T, V> u, T tMax)
    {
        DiffIterator<SegmentInterface<T, V>> itr = s.diffIterator();
        SegmentInterface<T, V> curr;

        while(itr.hasNext()) {
            curr = itr.next();
            T nextStart = tryPeekNext(itr, s.getEnd());

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
    }

    private static
    <T extends Comparable<T> & Serializable,
     V extends Comparable<V>,
     R extends Comparable<R>>
    void parallelExec(SegmentChain<T, V> s,
                      List<Update<T, R>> updates,
                      BiFunction<V, V, R> op,
                      Update<T, V> u)
    {
        DiffIterator<SegmentInterface<T, V>> itr = s.diffIterator();
        SegmentInterface<T, V> curr;

        while(itr.hasNext()) {
            curr = itr.next();
            if(curr.getStart().compareTo(u.getEnd()) < 0) {
                T nextStart = tryPeekNext(itr, s.getEnd());

                T end = min(nextStart, u.getEnd());
                T start = max(curr.getStart(), u.getStart());
                Update<T, R> r = new Update<>(start, end,
                        op.apply(u.getValue(), curr.getValue()));
                updates.add(r);
            }
        }
    }

    private static <T extends Comparable<T>> T max(T a, T b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    private static <T extends Comparable<T>> T min(T a, T b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    private static
    <T extends Comparable<T> & Serializable, V extends Comparable<V>>
    T tryPeekNext(DiffIterator<SegmentInterface<T, V>> itr, T oldValue) {
        try {
            return itr.peekNext().getStart();
        } catch (NoSuchElementException ignored) {
            return oldValue;
        }
    }
}
