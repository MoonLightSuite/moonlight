package eu.quanticol.moonlight.offline.signal.mfr;

import eu.quanticol.moonlight.offline.signal.ParallelSignalCursor1;
import eu.quanticol.moonlight.offline.signal.SignalCursor;

import java.util.Arrays;
import java.util.function.IntFunction;

public class MfrCursor<V> extends ParallelSignalCursor1<V> {
    private final int[] locations;

    public MfrCursor(int[] locations, IntFunction<SignalCursor<Double, V>> f) {
        super(locations, f);
        this.locations = locations;
    }

    @Override
    protected void initCursors(int[] locations,
                               IntFunction<SignalCursor<Double, V>> f) {
        Arrays.stream(locations).forEach(loc -> cursors.add(f.apply(loc)));
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
