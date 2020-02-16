/*******************************************************************************
 * MoonLight: a light-weight framework for runtime monitoring
 * Copyright (C) 2018 
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package eu.quanticol.moonlight.signal;

import eu.quanticol.moonlight.util.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class RecordHandler {

    private DataHandler<?>[] handlers;

    private Map<String, Integer> variables;


    public RecordHandler(DataHandler<?>... varTypes) {
        this(null, varTypes);
    }

    public RecordHandler(Map<String, Integer> variableIndex, DataHandler<?>... varTypes) {
        this.handlers = Arrays.copyOf(varTypes, varTypes.length);
        this.variables = variableIndex;
    }

    @SafeVarargs
    public static RecordHandler createFactory(Pair<String, DataHandler<?>>... variables) {
        DataHandler<?>[] dataHandlers = new DataHandler<?>[variables.length];
        Map<String, Integer> variableIndex = new HashMap<>();
        int counter = 0;
        for (Pair<String, DataHandler<?>> p : variables) {
            dataHandlers[counter] = p.getSecond();
            if (variableIndex.put(p.getFirst(), counter++) != null) {
                throw new IllegalArgumentException("Duplicated variable " + p.getFirst() + "!");
            }
        }
        return new RecordHandler(variableIndex, dataHandlers);
    }

    public Record fromObject(Object... values) {
        if (values.length != handlers.length) {
            throw new IllegalArgumentException("Wrong data size! (Expected " + handlers.length + " is " + values.length);
        }
        return build(IntStream.range(0, values.length).boxed().map(i -> handlers[i].fromObject(values[i])).toArray());
    }

    public Record fromDouble(double... values) {
        if (values.length != handlers.length) {
            throw new IllegalArgumentException("Wrong data size! (Expected " + handlers.length + " is " + values.length);
        }
        return build(IntStream.range(0, values.length).boxed().map(i -> handlers[i].fromDouble(values[i])).toArray());
    }

    private Record build(Object[] values) {
        return new Record(i -> handlers[i], values);
    }

    public Record fromString(String... strings) {
        if (strings.length != handlers.length) {
            throw new IllegalArgumentException("Wrong data size! (Expected " + handlers.length + " is " + strings.length + ")");
        }
        return build(IntStream.range(0, strings.length).boxed().map(i -> handlers[i].fromString(strings[i])).toArray());
    }

    public Record fromObject(Map<String, Object> values) {
        checkNumberOfVariables(values.size());
        Object[] data = new Object[handlers.length];
        values.forEach((v, o) -> {
            int variableIndex = getVariableIndex(v);
            if (variableIndex < 0) {
                throwUnknownVariableException(v);
            }
            DataHandler<?> handler = handlers[variableIndex];
            if (!handler.checkType(o)) {
                throwVariableTypeException(v, handler.getTypeOf().getTypeName(), o.getClass().getTypeName());
            }
            data[variableIndex] = handler.fromObject(o);
        });
        return build(data);
    }

    private void throwUnknownVariableException(String v) {
        throw new IllegalArgumentException("Unknown variable " + v);
    }

    private void throwVariableTypeException(String v, String expected, String actual) {
        throw new IllegalArgumentException("Wrong data type for variable" + v + "; expected " + expected + " is " + actual);

    }

    public int getVariableIndex(String v) {
        return variables.getOrDefault(v, -1);
    }

    public Record fromString(Map<String, String> values) {
        checkNumberOfVariables(values.size());
        Object[] data = new Object[handlers.length];
        values.forEach((v, o) -> {
            int variableIndex = getVariableIndex(v);
            if (variableIndex < 0) {
                throwUnknownVariableException(v);
            }
            DataHandler<?> handler = handlers[variableIndex];
            data[variableIndex] = handler.fromString(o);
        });
        return build(data);
    }

    public boolean checkNumberOfVariables(int size) {
        if (size != variables.size()) {
            throw new IllegalArgumentException("Wrong number of variables! (Expected " + handlers.length + " is " + size + ")");
        }
        return true;
    }

    public boolean checkVariableType(String v, String type) {
        int variableIndex = getVariableIndex(v);
        if (variableIndex < 0) {
            return false;
        }
        return handlers[variableIndex].checkTypeCode(type);
    }

    public Map<String, Integer> getVariableIndex() {
        return variables;
    }

    public int size() {
        return handlers.length;
    }

    public String getTypeCode(String name) {
        int variableIndex = getVariableIndex(name);
        if (variableIndex < 0) {
            return null;
        }
        return handlers[variableIndex].getTypeCode();
    }

    public String[] getVariables() {
        return variables.keySet().toArray(new String[variables.size()]);
    }

    public boolean checkValueFromString(String v, String value) {
        int variableIndex = getVariableIndex(v);
        if (variableIndex < 0) {
            return false;
        }
        return handlers[variableIndex].checkValueFromString(value);
    }

    public static Signal<Record> buildTemporalSignal(RecordHandler handler, double[] time,
                                                     String[][] signal) {
        Signal<Record> toReturn = new Signal<>();
        for (int i = 0; i < time.length; i++) {
            toReturn.add(time[i], handler.fromString(signal[i]));
        }
        return toReturn;
    }

    public static Signal<Record> buildTemporalSignal(RecordHandler handler, double[] time,
            Object[][] signal) {
    	Signal<Record> toReturn = new Signal<>();
    	for (int i = 0; i < time.length; i++) {
    		toReturn.add(time[i], handler.fromObject(signal[i]));
    	}
    	return toReturn;
    }

    public static Signal<Record> buildTemporalSignal(RecordHandler handler, double[] time,
                                                     double[][] signal) {
        Signal<Record> toReturn = new Signal<>();
        for (int i = 0; i < time.length; i++) {
            toReturn.add(time[i], handler.fromDouble(signal[i]));
        }
        return toReturn;
    }

    public static SpatioTemporalSignal<Record> buildSpatioTemporalSignal(int size, RecordHandler handler, double[] time,
                                                                         String[][][] signal) {
        return new SpatioTemporalSignal<>(size, i -> buildTemporalSignal(handler, time, signal[i]));
    }

    public static SpatioTemporalSignal<Record> buildSpatioTemporalSignal(int size, RecordHandler handler, double[] time,
            Object[][][] signal) {
    	return new SpatioTemporalSignal<>(size, i -> buildTemporalSignal(handler, time, signal[i]));
    }

    public static SpatioTemporalSignal<Record> buildSpatioTemporalSignal(int size, RecordHandler handler, double[] time,
                                                                         double[][][] signal) {
        return new SpatioTemporalSignal<>(size, i -> buildTemporalSignal(handler, time, signal[i]));
    }

}
