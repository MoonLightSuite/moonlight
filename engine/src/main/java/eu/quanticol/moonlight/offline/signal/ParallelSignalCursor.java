/**
 *
 */
package eu.quanticol.moonlight.offline.signal;

import java.util.function.IntFunction;

/**
 * @author loreti
 */
public class ParallelSignalCursor<V> extends ParallelSignalCursor1<V> implements
        SignalCursor<Double, IntFunction<V>> {


    public ParallelSignalCursor(int size,
                                IntFunction<SignalCursor<Double, V>> f) {
        super(size, f);
    }

    @Override
    protected void initCursors(int[] locations,
                               IntFunction<SignalCursor<Double, V>> f) {
        for (int i = 0; i < locations.length; i++) {
            cursors.add(f.apply(i));
        }
    }

}
