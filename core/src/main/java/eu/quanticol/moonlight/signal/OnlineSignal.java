package eu.quanticol.moonlight.signal;

import eu.quanticol.moonlight.domain.IntervalExtremes;
import eu.quanticol.moonlight.domain.Intervallable;

import java.util.function.Function;

public class OnlineSignal<T extends Intervallable<T>> extends Signal<T> {

    public OnlineSignal() {
        super();
    }


    public <R extends IntervalExtremes<? extends Comparable<?>>> Signal<R> apply2(Function<T, R> f, R data) {
        Signal<R> newSignal = super.apply(f);
        if(newSignal.isEmpty()) {
            //return newSignal.add(0, IntervalExtremes::any);
        }
        return newSignal;
    }
}
