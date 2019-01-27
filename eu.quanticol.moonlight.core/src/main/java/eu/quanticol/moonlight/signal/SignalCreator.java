package eu.quanticol.moonlight.signal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SignalCreator<T, P> {

    private Map<String, Function<T, P>> functionalMap;

    public SignalCreator(Map<String, Function<T, P>> functionalMap) {
        this.functionalMap = functionalMap;
    }

    public VariableArraySignal generate(double timeInit, double timeEnd, double timeStep) {
        List<Class<?>> varTypes = new ArrayList<>();
        List<String> varName = new ArrayList<>();
        for (Map.Entry<String, Function<T, P>> stringFunctionEntry : functionalMap.entrySet()) {
            Function value = stringFunctionEntry.getValue();
            varTypes.add(value.apply(timeInit).getClass());
            varName.add(stringFunctionEntry.getKey());
        }

        Class<?>[] classes = varTypes.toArray(new Class<?>[0]);
        String[] names = varName.toArray(new String[0]);
        VariableArraySignal result = new VariableArraySignal(names, new AssignmentFactory(classes));
        for (double t = timeInit; t < timeEnd; t += timeStep) {
            result.add(t, applyFunctions(functionalMap.entrySet().iterator(), classes, t));
        }
        return result;
    }

    private Assignment applyFunctions(Iterator<Map.Entry<String, Function<T, P>>> iterator, Class<?>[] classes, double t) {
        Object[] values = new Object[classes.length];

        for (int i = 0; i < classes.length; i++) {
            Map.Entry<String, Function<T, P>> next = iterator.next();
            Function value = next.getValue();
            values[i] = value.apply(t);
        }
        return new Assignment(classes, values);
    }

    public String[] getVariableNames() {
        return functionalMap.keySet().toArray(new String[0]);

    }
}
