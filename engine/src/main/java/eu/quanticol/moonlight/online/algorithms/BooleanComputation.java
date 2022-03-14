package eu.quanticol.moonlight.online.algorithms;

import eu.quanticol.moonlight.core.signal.Sample;
import eu.quanticol.moonlight.online.signal.*;

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
        List<Sample<T, R>> ls =
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
        List<Sample<T, R>> ls =
            us.stream()
              .map(s -> new TimeSegment<>(s.getStart(), op.apply(s.getValue())))
              .collect(Collectors.toList());


        return new TimeChain<>(ls, us.getEnd());
    }

    /**
     * TODO: this should be different for left and right operands
     * @param c1 input signal of first argument
     * @param us updates of second argument
     * @param op operation to be performed (e.g. and/or)
     * @param <T> time domain of the signals
     * @param <R> evaluation domain of the semantics
     * @return a chain of sequential updates
     */
    public static
    <T extends Comparable<T> & Serializable, R>
    TimeChain<T, R> binarySequence(TimeChain<T, R> c1,
                                   TimeChain<T, R> us,
                                   BinaryOperator<R> op)
    {
        List<Sample<T, R>> updates = new ArrayList<>(us.size());
        ChainsCombinator<T, R> itr = new ChainsCombinator<>(c1, us);
        itr.forEach((segment, update) -> binaryOp(segment, update, updates, op));
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

    private static
    <T extends Comparable<T> & Serializable, R>
    List<Update<T, R>> rightApply(TimeChain<T, R> s,
                    BinaryOperator<R> op, Update<T, R> u)
    {
        List<Update<T, R>> updates = new ArrayList<>();
        ChainIterator<Sample<T, R>> itr = s.chainIterator();
        Sample<T, R> curr;
        T nextTime;

        while(itr.hasNext()) {
            curr = itr.next();
            nextTime = tryPeekNextStart(itr, s.getEnd());
            exec(curr, u, nextTime, op, updates);
        }

        return updates;
    }

    private static <T extends Comparable<T> & Serializable, R>
    void exec(Sample<T, R> curr, Update<T, R> u, T nextTime,
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

    private static <T extends Comparable<T> & Serializable, R>
    void binaryOp(Sample<T, R> left,
                  Sample<T, R> right,
                  List<Sample<T, R>> output,
                  BinaryOperator<R> op)
    {
        T start = max(left, right).getStart();
        R value = op.apply(right.getValue(), left.getValue());
        Sample<T, R> r = new TimeSegment<>(start, value);
        int last = output.size() - 1;
        if(output.isEmpty() || !output.get(last).getValue().equals(value))
            output.add(r);
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
    T tryPeekNextStart(ChainIterator<Sample<T, V>> itr, T defaultValue)
    {
        if(itr.hasNext())
            return itr.peekNext().getStart();
        else
            return defaultValue;
    }
}
