package eu.quanticol.moonlight.online.algorithms;

import eu.quanticol.moonlight.core.formula.Interval;
import eu.quanticol.moonlight.online.signal.TimeChain;
import eu.quanticol.moonlight.online.signal.Update;

import java.util.*;
import java.util.function.BinaryOperator;

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

    public static <R>
    List<Update<Double, R>> slidingWindow(TimeChain<Double, R> s,
                                          Update<Double, R> u,
                                          Interval opHorizon,
                                          BinaryOperator<R> op)
    {
        return new SlidingWindow<>(s, u, opHorizon, op).run();
    }

    public static <R>
    List<TimeChain<Double, R>> slidingWindow(TimeChain<Double, R> s,
                                             TimeChain<Double, R> us,
                                             Interval opHorizon,
                                             BinaryOperator<R> op)
    {
        return new SlidingWindow<>(s, us, opHorizon, op).runChain();
    }

}
