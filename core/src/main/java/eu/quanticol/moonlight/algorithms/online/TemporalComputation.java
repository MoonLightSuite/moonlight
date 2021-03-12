package eu.quanticol.moonlight.algorithms.online;

import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.signal.online.DiffIterator;
import eu.quanticol.moonlight.signal.online.SegmentChain;
import eu.quanticol.moonlight.signal.online.SegmentInterface;
import eu.quanticol.moonlight.signal.online.Update;

import java.io.Serializable;
import java.util.*;
import java.util.function.BinaryOperator;

import static eu.quanticol.moonlight.algorithms.online.BooleanComputation.tryPeekNextStart;

/**
 * Note that the methods in this class require explicit time declaration to deal
 * with numeric operations which cannot be defined on Generic types, not even
 * on the <code>Number</code> class.
 *
 * That's because numeric operations (such as +, -, *, /, %) are defined on
 * primitive types, and Generic types do not support instantiations based on
 * primitive types.
 *
 * @see <a href="https://docs.oracle.com/javase/tutorial/java/generics/restrictions.html">Java Generics Restrictions</a>
 */
public class TemporalComputation {

    private TemporalComputation() {}    // hidden constructor

    public static <R extends Comparable<R>>
    List<Update<Double, R>> slidingWindow(SegmentChain<Double, R> s,
                                          Update<Double, R> u,
                                          Interval opHorizon,
                                          BinaryOperator<R> op)
    {
        return new SlidingWindow<>(s, u, opHorizon, op).slide();
    }

}
