package eu.quanticol.moonlight.formula;

import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SignalCursor;

import java.util.function.BinaryOperator;

public class OnlineSlidingWindow<R> extends SlidingWindow<R> {
    private final R undefined;

    private SignalCursor<R> previousCursor;
    private Window previousWindow;
    private double horizon;

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
                               R undefined,
                               double horizon)
    {
        super(a, b, aggregator, isFuture);
        this.undefined = undefined;
        this.horizon = horizon;
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

        /*if(s.isEmpty() ) {
                //|| (s.end() - s.start() < size())) {
            Signal<R> o = new Signal<>();
            o.add(s.start(), undefined);
            o.endAt(s.end());
            return o;
        }*/

        // We prepare the Sliding Window
        SignalCursor<R> cursor = loadCursor(s);
        Window window = loadWindow();

        // We actually slide the window
        Signal<R> result = doSlide(cursor, window);

        // If we have no results, we slided to an undefined area
        // from the very beginning
        if(result.isEmpty()) {
            Signal<R> o = new Signal<>();
            o.add(s.start(), undefined);
            o.endAt(Math.max(s.end(), horizon));
            return o;
        }

        // We store the final value of the window
        storeEnding(result, window);

        if (result.getEnd() < horizon) {
            result.add(result.getEnd(), undefined);
            result.endAt(horizon);
        }

        return result;
    }

    private Window loadWindow() {
        if(previousWindow ==  null)
            previousWindow = new Window();

        return previousWindow;
    }

    private SignalCursor<R> loadCursor(Signal<R> s) {
        if(previousCursor == null)
            previousCursor = iteratorInit(s);

        return previousCursor;
    }
}
