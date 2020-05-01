package eu.quanticol.moonlight.examples.subway.statistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

public class SignalStatistics<T> {
    private final Collection<T> results = Collections.synchronizedCollection(new ArrayList<>());
    private final Collection<Double> durations = new ArrayList<>();

    public T record(Supplier<T> f) {
        try {
            long startingTime = System.currentTimeMillis();
            T result = f.get(); //Supplier code execution (i.e. f.apply())
            long endingTime = System.currentTimeMillis();

            double duration = (endingTime - startingTime) / 1000.0;

            durations.add(duration);
            results.add(result);
            return result;
        } catch(Exception e) {
            System.out.println("ERROR: computation failed");
            e.printStackTrace();
            return null;
        }
    }

    public Collection<T> getResults() {
        return results;
    }
}
