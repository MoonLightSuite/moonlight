package eu.quanticol.moonlight.offline;

import eu.quanticol.moonlight.offline.signal.OfflineSignalCursor;
import eu.quanticol.moonlight.offline.signal.Segment;
import eu.quanticol.moonlight.offline.signal.Signal;
import eu.quanticol.moonlight.offline.signal.SignalCursor;
import eu.quanticol.moonlight.offline.signal.mfr.MfrSignal;

import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestSignalUtils {


    public static List<SignalCursor<Double, Boolean>> basicCursorsWithOffset() {
        var forward = true;
        var first1 = new Segment<>(0.0, true);
        var second1 = new Segment<>(5.0, false);
        var first2 = new Segment<>(2.0, false);
        var second2 = new Segment<>(10.0, false);
        var c1 = new OfflineSignalCursor<>(forward, first1, second1);
        var c2 = new OfflineSignalCursor<>(forward, first2, second2);
        return List.of(c1, c2, c1);
    }


    public static MfrSignal<Integer> basicSignal(int totalLocations,
                                                 int[] locationsSet) {
        var timeSignals = someTimeSignals(totalLocations);
        return new MfrSignal<>(totalLocations, timeSignals::get, locationsSet);
    }

    public static List<Signal<Integer>> someTimeSignals(int totalLocations) {
        List<Signal<Integer>> stSignal = mock(List.class);
        Signal<Integer> signal = new Signal<>();
        signal.add(0.0, 2);
        signal.add(2.0, 3);
        IntStream.range(0, totalLocations)
                .forEach(i -> when(stSignal.get(i)).thenReturn(signal));
        return stSignal;
    }
}
