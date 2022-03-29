package eu.quanticol.moonlight.core.io;

public interface SerializableValues<R> {
    R valueOf(boolean b);

    R valueOf(double v);

    default R valueOf(int v) {
        return valueOf((double) v);
    }
}
