package eu.quanticol.moonlight.core.base;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * http://stackoverflow.com/questions/3642452/java-n-tuple-implementation/3642623#3642623
 */
class TupleImpl implements Tuple {
    private final TupleType type;
    private final Comparable<?>[] values;

    TupleImpl(TupleType type, Comparable<?>[] values) {
        this.type = type;
        if (values == null || values.length == 0) {
            this.values = new Comparable<?>[0];
        } else {
            this.values = new Comparable<?>[values.length];
            System.arraycopy(values, 0, this.values, 0, values.length);
        }
    }

    @Override
    public TupleType getType() {
        return type;
    }

    @Override
    public int size() {
        return values.length;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getIthValue(int i) {
        return (T) values[i];
    }

    @Override
    public boolean equals(Object object) {
        if (object == null)   return false;
        if (this == object)   return true;

        if (! (object instanceof Tuple))   return false;

        final Tuple other = (Tuple) object;
        if (other.size() != size())   return false;

        final int size = size();
        for (int i = 0; i < size; i++) {
            final Object thisIthValue = getIthValue(i);
            final Object otherIthValue = other.getIthValue(i);
            if ((thisIthValue == null && otherIthValue != null) ||
                (thisIthValue != null && !thisIthValue.equals(otherIthValue))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int compareTo(@NotNull Tuple o) {
        int bigger = 0;
        int smaller = 0;
        for(int i = 0; i < values.length; i++) {
            if(values[i].compareTo(o.getIthValue(i)) > 0)
                bigger++;
            else if(values[i].compareTo(o.getIthValue(i)) < 0)
                smaller++;
        }
        if(bigger == values.length) return 1;
        if(smaller == values.length) return -1;
        return 0;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        for (Object value : values) {
            if (value != null) {
                hash = hash * 37 + value.hashCode();
            }
        }
        return hash;
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}