package io.github.moonlightsuite.moonlight.offline.signal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SignalCreator {

    private Map<String, Function<Double, ?>> functionalMap;
    private RecordHandler factory;

    public SignalCreator(RecordHandler factory, Map<String, Function<Double, ?>> functionalMap) {
        this.functionalMap = functionalMap;
        this.factory = factory;
    }

    public VariableArraySignal generate(double timeInit, double timeEnd, double timeStep) {
        VariableArraySignal result = new VariableArraySignal(factory);
        for (double t = timeInit; t < timeEnd; t += timeStep) {
            result.addFromMap(t, applyFunctions(t));
        }
        return result;
    }

    public double[] generateTime(double timeInit, double timeEnd, double timeStep) {
        int length = (int) Math.round(Math.floor((timeEnd - timeInit) / timeStep));
        double[] time = new double[length];
        for (int i = 0; i < time.length; i++) {
            time[i] = timeInit + i * timeStep;
        }
        return time;
    }

    private Map<String,Object> applyFunctions(double t) {
    	HashMap<String,Object> toReturn = new HashMap<>();
    	functionalMap.forEach((v,f) -> toReturn.put(v, f.apply(t)));
    	return toReturn;
    }

    public String[] getVariableNames() {
        return functionalMap.keySet().toArray(new String[0]);

    }
}
