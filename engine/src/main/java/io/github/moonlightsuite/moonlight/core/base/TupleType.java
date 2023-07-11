package io.github.moonlightsuite.moonlight.core.base;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a type of tuple.  Used to define a type of tuple and then
 * create tuples of that type.
 *
 * http://stackoverflow.com/questions/3642452/java-n-tuple-implementation/3642623#3642623
 */
public interface TupleType {

    /**
     * @return the size of the tuple
     */
    int size();

    /**
     * @param i i-th dimension of the tuple
     * @return the typed value of the i-th dimension
     */
    Class<?> getIthType(int i);

    /**
     * Factory method to create a tuple from the current type.
     * Tuples should contain only immutable objects or
     * objects that won't be modified while part of a tuple.
     *
     * @param values values of the tuple
     * @return Tuple with the given values
     * @throws IllegalArgumentException if the wrong # of arguments or
     *                                  incompatible tuple values are provided
     */
    Tuple createTuple(@NotNull Comparable<?>... values);

    /**
     * @param types passed types of the tuple type to be generated
     * @return a tuple type, generated from the passed types in the given order
     */
    @SafeVarargs
    static @NotNull TupleType of(@NotNull Class<? extends Comparable<?>>... types) {
        return new TupleTypeImpl(types);
    }
}
