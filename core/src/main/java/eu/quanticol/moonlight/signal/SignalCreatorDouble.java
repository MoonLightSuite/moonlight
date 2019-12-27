package eu.quanticol.moonlight.signal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SignalCreatorDouble {

//    private Map<String, Function<Double, Double>> functionalMap;
//
//    public SignalCreatorDouble(Map<String, Function<Double, Double>> functionalMap) {
//        this.functionalMap = functionalMap;
//    }
//
//    public VariableArraySignal generate(double timeInit, double timeEnd, double timeStep) {
//        List<Class<?>> varTypes = new ArrayList<>();
//        List<String> varName = new ArrayList<>();
//        for (Map.Entry<String, Function<Double, Double>> stringFunctionEntry : functionalMap.entrySet()) {
//            Function value = stringFunctionEntry.getValue();
//            varTypes.add(value.apply(timeInit).getClass());
//            varName.add(stringFunctionEntry.getKey());
//        }
//
//        Class<?>[] classes = varTypes.toArray(new Class<?>[0]);
//        String[] names = varName.toArray(new String[0]);
//        VariableArraySignal result = new VariableArraySignal(names, new AssignmentFactory(classes));
//        for (double t = timeInit; t < timeEnd; t += timeStep) {
//            result.add(t, applyFunctions(functionalMap.entrySet().iterator(), classes, t));
//        }
//        result.add(timeEnd, applyFunctions(functionalMap.entrySet().iterator(), classes, timeEnd));
//
//        return result;
//    }
//
//    public double[] generateTime(double timeInit, double timeEnd, double timeStep) {
//        int length = (int) Math.round(Math.floor((timeEnd - timeInit) / timeStep));
//        double[] time = new double[length];
//        for (int i = 0; i < time.length; i++) {
//            time[i] = timeInit + i * timeStep;
//        }
//        return time;
//    }
//
//    public double[][] generateValues(double[] time) {
//        double[][] values = new double[functionalMap.keySet().size()][time.length];
//        for (int i = 0; i < time.length; i++) {
//            Iterator<Function<Double, Double>> iterator = functionalMap.values().iterator();
//            for (int j = 0; j < functionalMap.keySet().size(); j++) {
//                Function<Double, Double> next = iterator.next();
//                values[j][i] = next.apply(time[i]);
//            }
//        }
//        return values;
//    }
//
//
//    private Assignment applyFunctions(Iterator<Map.Entry<String, Function<Double,Double>>> iterator, Class<?>[] classes, double t) {
//        Object[] values = new Object[classes.length];
//
//        for (int i = 0; i < classes.length; i++) {
//            Map.Entry<String, Function<Double,Double>> next = iterator.next();
//            Function value = next.getValue();
//            values[i] = value.apply(t);
//        }
//        return new Assignment(classes, values);
//    }
//
//    public String[] getVariableNames() {
//        return functionalMap.keySet().toArray(new String[0]);
//
//    }
}
