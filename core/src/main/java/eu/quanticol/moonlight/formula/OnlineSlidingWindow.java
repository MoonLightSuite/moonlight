package eu.quanticol.moonlight.formula;

import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SignalCursor;

import java.util.function.BinaryOperator;

public class OnlineSlidingWindow<R> extends SlidingWindow<R> {
    private final R undefined;

    /**
     * Constructs a Sliding Window on the given aggregator and time interval.
     *
     * @param a          beginning of the interval of interest
     * @param b          ending of the interval of interest
     * @param aggregator the aggregation function the Sliding Window will use
     * @param isFuture   flag to tell whether the direction of the sliding
     */
    public OnlineSlidingWindow(double a, double b,
                               BinaryOperator<R> aggregator,
                               boolean isFuture,
                               R undefined)
    {
        super(a, b, aggregator, isFuture);
        this.undefined = undefined;
    }

    /**
     * Activates the actual shift of the Signal
     * @param s the Signal to be shifted
     * @return the shifted Signal
     */
    @Override
    public Signal<R> apply(Signal<R> s) {
        // If the signal is empty or shorter than the time horizon,
        // we return an empty signal
        //if (s.isEmpty()) {
        //    return new Signal<>();
        //}

        if(s.isEmpty()) {
            Signal<R> o = new Signal<>();
            o.add(s.start() + getA(), undefined);
            return o;
        }

        // We prepare the Sliding Window
        SignalCursor<R> cursor = iteratorInit(s);
        Window window = new Window();

        // We actually slide the window
        Signal<R> result = doSlide(cursor, window);

        // If we have no results, we slided to an undefined area from the very
        // beginning
        if(result.isEmpty()) {
            Signal<R> o = new Signal<>();
            o.add(s.start() + getA(), undefined);
            return o;
        }

        // We store the final value of the window
        storeEnding(result, window);

        return result;
    }
}
