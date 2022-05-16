package eu.quanticol.moonlight.offline.signal.mfr;

import eu.quanticol.moonlight.offline.signal.ParallelSignalCursor;
import eu.quanticol.moonlight.offline.signal.SignalCursor;

import java.util.Arrays;
import java.util.function.IntFunction;

public class MfrCursor<V> extends ParallelSignalCursor<V> {
    private final int[] locations;

    public MfrCursor(int[] locations, IntFunction<SignalCursor<Double, V>> f) {
        super(locations.length, f);
        this.locations = locations;
    }

    @Override
    protected SignalCursor<Double, V> getCursorAtLocation(int location) {
        int position = hasLocation(location);
        if (position >= 0) {
            return getCursors().get(position);
        }
        var error = "The cursor is not defined at the given location";
        throw new IllegalArgumentException(error);
    }

    private int hasLocation(int location) {
        return Arrays.binarySearch(locations, location);
    }
}
