package eu.quanticol.moonlight.examples.subway.statistics;

import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

public class SignalStatistics<T extends SpatialTemporalSignal> {
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

    /**
     * Executes the computations and returns a DTO with the final statistics
     * @return a DTO containing the final statistics
     */
    public Statistics analyze() {
        double avg = computeAverage();
        double var = computeVariance(avg);
        double stdDev = Math.sqrt(var);
        double execTime = computeAvgExecTime();
        int cnt = results.size();

        return new Statistics(avg, execTime, stdDev, var, cnt);
    }

    private double computeAverage() {
        double value = 0;

        for (SpatialTemporalSignal<?> result : results) {
            Signal<?> s = result.getSignals().get(0);
            if (s.valueAt(0) instanceof Double) {
                value += (Double) s.valueAt(0);
            } else if (s.valueAt(0) instanceof Boolean) {
                value += (Boolean) s.valueAt(0) ? 1 : 0;
            }
            else
                throw new InvalidParameterException("Unknown Signal Output");
        }

        return value / results.size();
    }

    private double computeAvgExecTime() {
        double t = 0;

        for (Double duration : durations) {
            t += duration;
        }

        return t / durations.size();
    }

    private double computeVariance(double avg) {
        double value = 0;

        for (SpatialTemporalSignal<?> result : results) {
            Signal<?> s = result.getSignals().get(0);
            if (s.valueAt(0) instanceof Double) {
                double v = (Double) s.valueAt(0);
                value += Math.pow((v - avg), 2);
            } else if (s.valueAt(0) instanceof Boolean) {
                double v = (Boolean) s.valueAt(0) ? 1 : 0;
                value += Math.pow((v - avg), 2);
            }
            else
                throw new InvalidParameterException("Unknown Signal Output");
        }

        return value / (results.size() - 1);
    }



    public static class Statistics implements Serializable {

        Statistics(double avg, double exec, double std, double var, int cnt) {
            average = avg;
            executionTime = exec;
            stdDeviation = std;
            variance = var;
            count = cnt;
        }

        /**
         * Average of the results of the monitoring process
         */
        public final double average;

        /**
         * Standard deviation of the results of the monitoring process
         */
        public final double stdDeviation;

        /**
         * Variance of the results of the monitoring process
         */
        public final double variance;

        /**
         * Average execution time of the monitoring process
         */
        public final double executionTime;

        /**
         * Number of executions of the monitoring process
         */
        public final int count;

        @Override
        public String toString() {
            return  "AVG:" + average + " | STD:" + stdDeviation +
                    " | VAR:" + variance + " | CNT:" + count +
                    " | AVG_EXEC_TIME:" + executionTime + "ms";
        }
    }
}
