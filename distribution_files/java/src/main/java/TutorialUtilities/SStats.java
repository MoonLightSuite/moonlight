package TutorialUtilities;

import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

public class SStats<T extends SpatialTemporalSignal> {
    private final Collection<T> results = Collections.synchronizedCollection(new ArrayList<>());
    private final Collection<Float> durations = new ArrayList<>();

    private final long startingTime;

    public SStats() {
        startingTime = System.currentTimeMillis();
    }

    public T record(Supplier<T> f) {
        try {
            long startingTime = System.currentTimeMillis();
            T result = f.get(); //Supplier code execution (i.e. f.apply())
            long endingTime = System.currentTimeMillis();

            float duration = (float)((endingTime - startingTime) / 1000.0);

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
        float avg = computeAverage();
        float var = computeVariance(avg);
        float stdDev = (float) Math.sqrt(var);
        float execTime = computeAvgExecTime();
        int cnt = results.size();

        long endingTime = System.currentTimeMillis();
        float totalTime = (float) ((endingTime - startingTime) / 1000.0);

        return new Statistics(avg, execTime, stdDev, var, cnt, totalTime);
    }

    private float computeAverage() {
        float value = 0;

        for (SpatialTemporalSignal<?> result : results) {
            Signal<?> s = result.getSignals().get(0);
            if (s.valueAt(0) instanceof Float) {
                value += (Float) s.valueAt(0);
            } else if (s.valueAt(0) instanceof Boolean) {
                value += (Boolean) s.valueAt(0) ? 1 : 0;
            }
            else
                throw new InvalidParameterException("Unknown Signal Output");
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

        for (SpatialTemporalSignal<?> result : results) {
            Signal<?> s = result.getSignals().get(0);
            if (s.valueAt(0) instanceof Float) {
                float v = (Float) s.valueAt(0);
                value += Math.pow((v - avg), 2);
            } else if (s.valueAt(0) instanceof Boolean) {
                float v = (Boolean) s.valueAt(0) ? 1 : 0;
                value += Math.pow((v - avg), 2);
            }
            else
                throw new InvalidParameterException("Unknown Signal Output");
        }

        return value / (results.size());
    }



    public static class Statistics implements Serializable {

        Statistics(float avg, float exec, float std, float var, int cnt, float tot) {
            average = avg;
            executionTime = exec;
            stdDeviation = std;
            variance = var;
            count = cnt;
            totalExecutionTime = tot;
        }

        /**
         * Average of the results of the monitoring process
         */
        public final float average;

        /**
         * Standard deviation of the results of the monitoring process
         */
        public final float stdDeviation;

        /**
         * Variance of the results of the monitoring process
         */
        public final float variance;

        /**
         * Average execution time of the monitoring process
         */
        public final float executionTime;

        /**
         * Number of executions of the monitoring process
         */
        public final int count;

        /**
         * Total execution time of the monitoring process
         */
        public final float totalExecutionTime;

        @Override
        public String toString() {
            return  "AVG:" + average + " | STD:" + stdDeviation +
                    " | VAR:" + variance + " | CNT:" + count +
                    " | AVG_EXEC_TIME:" + executionTime + "ms" +
                    " | TOT_EXEC_TIME:" + totalExecutionTime + "ms";
        }
    }
}
