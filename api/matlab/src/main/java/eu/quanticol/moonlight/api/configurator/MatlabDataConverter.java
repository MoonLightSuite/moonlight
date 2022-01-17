package eu.quanticol.moonlight.api.configurator;

import java.lang.reflect.Array;

public class MatlabDataConverter {
    private MatlabDataConverter() {
        //utility class
    }

    public static <T> T[] getArray(Object[] array, Class<T> object) {
        T[] ts = (T[]) Array.newInstance(object, array.length);
        for (int i = 0; i < array.length; i++) {
            ts[i] = (T) array[i];
        }
        return ts;
    }
}