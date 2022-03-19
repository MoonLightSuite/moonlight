package eu.quanticol.moonlight.offline.algorithms;

import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.offline.signal.Signal;

import java.util.function.BinaryOperator;

public class UnboundedOperator<T> {
    private final SignalDomain<T> domain;
    private T current;

    UnboundedOperator(SignalDomain<T> domain) {
        this.domain = domain;
    }

    public Signal<T> computeUnbounded(Signal<T> s1, Signal<T> s2,
                                      boolean isForward)
    {
        setCurrent(domain.min());
        BooleanOp<T, T> booleanOp =  new BooleanOp<>(isForward);
        BinaryOperator<T> op = (left, right) -> {
            T value = untilOp(left, right, getCurrent());
            setCurrent(value);
            return value;
        };
        return booleanOp.applyBinary(s1, op, s2);
    }

    private T getCurrent() {
        return current;
    }

    private void setCurrent(T value) {
        current = value;
    }

    private T untilOp(T left, T right, T previous) {
        //TODO: instead of right,
        // 		should be domain.conjunction(right, left)
        //		in order to comply with paper's until definition
        return domain.disjunction(right, domain.conjunction(left, previous));
    }


}
