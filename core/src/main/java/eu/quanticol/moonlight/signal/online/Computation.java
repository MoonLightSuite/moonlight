package eu.quanticol.moonlight.signal.online;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Computation {

    public static
    <T extends Comparable<T>, V extends Comparable<V>, R extends Comparable<R>>
    Update<T, R> unary(Update<T, V> u, Function<V, R> op)
    {
        return new Update<>(u.getStart(), u.getEnd(), op.apply(u.getValue()));
    }

    public static
    <V extends Comparable<V>, R extends Comparable<R>>
    List<Update<Double, R>> binary(
            SignalInterface<Double, V> s,
            Update<Double, V> u1, Update<Double, V> u2,
            BiFunction<V, V, R> op)
    {
        List<Update<Double, R>> updates = new ArrayList<>();
        if(u1.getEnd().compareTo(u2.getStart()) < 0) {
            Update<Double, R> u = new Update<>(u1.getStart(), u1.getEnd(),
                    op.apply(s.getValueAt(u1.getStart()), u1.getValue()));
            updates.add(u);
        } else if(u2.getEnd().compareTo(u1.getStart()) < 0) {

        } //overlap cases

        return updates;
    }
}
