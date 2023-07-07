package io.github.moonlightsuite.moonlight.core.base;

/**
 * Tuple are immutable objects.  Tuples should contain only immutable objects or
 * objects that won't be modified while part of a tuple.
 *
 * @see
 * <a href="https://stackoverflow.com/questions/3642452/java-n-tuple-implementation/3642623#3642623">
 *     Java N-tuple Implementation | Stack Overflow
 * </a>
 */
public interface Tuple extends Comparable<Tuple> {

    /**
     * @return the type of the tuple
     */
    TupleType getType();

    /**
     * @return the arity of the tuple
     */
    int size();

    /**
     * @param i the i-th element of interest of the tuple
     * @param <T> the return type of the element
     * @return the typed i-th value
     */
    <T> T getIthValue(int i);

    /**
     * Generates a tuple given a type and an array of values
     * @param type type of the tuple
     * @param values set of values
     * @return a tuple of the given type containing the given values
     */
    static Tuple of(TupleType type, Comparable<?>... values) {
        return type.createTuple(values);
    }

//    TODO: would be nice to have constructors for simple tuples,
//          but this would conflict with current meaning of Pair and Triple as
//          it would require to implement Comparable.
//    static
//    <F extends Comparable<F>, S extends Comparable<S>>
//    Pair<F, S> of(F value1, S value2) {
//        return new Pair<>(value1, value2);
//    }
//
//    static
//    <F extends Comparable<F>, S extends Comparable<S>, T extends Comparable<T>>
//    Triple<F, S, T> of(F value1, S value2, T value3) {
//        return new Triple<>(value1, value2, value3);
//    }
}
