package io.github.moonlightsuite.moonlight.core.base;


/**
 * http://stackoverflow.com/questions/3642452/java-n-tuple-implementation/3642623#3642623
 */
class TupleTypeImpl implements TupleType {

    final Class<?>[] types;

    TupleTypeImpl(Class<? extends Comparable<?>>[] types) {
        this.types = (types != null ? types : new Class<?>[0]);
    }

    public int size() {
        return types.length;
    }

    //WRONG: <T> Class<T> getIthType(int i)
    public Class<?> getIthType(int i) {
        return types[i];
    }

    public Tuple createTuple(Comparable<?>... values) {
        if ((values == null && types.length == 0) ||
                (values != null && values.length != types.length)) {
            throw new IllegalArgumentException(
                    "Expected " + types.length + " values, not " +
                            (values == null ? "(null)" : values.length) + " values");
        }

        if (values != null) {
            for (int i = 0; i < types.length; i++) {
                final Class<?> ithType = types[i];
                final Object ithValue = values[i];
                if (ithValue != null &&
                        !ithType.isAssignableFrom(ithValue.getClass())) {
                    throw new IllegalArgumentException(
                            "Expected value #" + i + " ('" +
                                    ithValue + "') of new Tuple to be " +
                                    ithType + ", not " +
                                    ithValue.getClass());
                }
            }
        }

        return new TupleImpl(this, values);
    }
}
