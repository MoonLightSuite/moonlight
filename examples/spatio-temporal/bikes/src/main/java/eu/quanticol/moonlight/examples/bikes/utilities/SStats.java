package eu.quanticol.moonlight.examples.bikes.utilities;

import eu.quanticol.moonlight.offline.signal.Signal;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.statistics.SignalStatistics.Statistics;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

public class SStats<T> {
    private final Collection<T> results = Collections.synchronizedCollection(new ArrayList<>());
    private final Collection<Float> durations = new ArrayList<>();

    private long startingTime;
    private long endingTime;

    public SStats() {
        startingTime = System.currentTimeMillis();
    }

    public T record(Supplier<T> f) {
        try {
            startingTime = System.currentTimeMillis();
            T result = f.get(); //Supplier code execution (i.e. f.apply())
            endingTime = System.currentTimeMillis();

            float duration = (float) ((endingTime - startingTime) / 1000.0);

            durations.add(duration);
            results.add(result);
            return result;
        } catch (Exception e) {
            System.out.println("ERROR: computation failed");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Executes the computations and returns a DTO with the final statistics
     *
     * @return a DTO containing the final statistics
     */
    public Statistics analyze() {
        float avg = computeAverage();
        float var = computeVariance(avg);
        float stdDev = (float) Math.sqrt(var);
        float execTime = computeAvgExecTime();
        int cnt = results.size();

        endingTime = System.currentTimeMillis();
        float totalTime = (float) ((endingTime - startingTime) / 1000.0);

        return new Statistics(avg, execTime, stdDev, var, cnt, totalTime);
    }

    private float computeAverage() {
        float value = 0;

        for (T result : results) {
            try {
                SpatialTemporalSignal<?> r = (SpatialTemporalSignal<?>) result;
                Signal<?> s = r.getSignals().get(0);
                if (s.getValueAt(0.0) instanceof Float) {
                    value += (Float) s.getValueAt(0.0);
                } else if (s.getValueAt(0.0) instanceof Boolean) {
                    value += (Boolean) s.getValueAt(0.0) ? 1 : 0;
                } else
                    throw new InvalidParameterException("Unknown Signal Output");
            } catch (ClassCastException e) {
                return 0; // Unsupported for other types
            }
        }

        return value / results.size();
    }

    private float computeAvgExecTime() {
        float t = 0;

        for (Float duration : durations) {
            t += duration;
        }

        return t / durations.size();
    }

    private float computeVariance(float avg) {
        float value = 0;

        for (T result : results) {
            try {
                SpatialTemporalSignal<?> r = (SpatialTemporalSignal<?>) result;
                Signal<?> s = r.getSignals().get(0);
                if (s.getValueAt(0.0) instanceof Float) {
                    float v = (Float) s.getValueAt(0.0);
                    value += Math.pow((v - avg), 2);
                } else if (s.getValueAt(0.0) instanceof Boolean) {
                    float v = (Boolean) s.getValueAt(0.0) ? 1 : 0;
                    value += Math.pow((v - avg), 2);
                } else
                    throw new InvalidParameterException("Unknown Signal Output");
            } catch (ClassCastException e) {
                return 0; // Not supported on other types
            }
        }

        return value / (results.size());
    }
}
