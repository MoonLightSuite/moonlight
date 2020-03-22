/*
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
 */
package eu.quanticol.moonlight.signal;

import eu.quanticol.moonlight.util.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class RecordHandler implements DataHandler<Record> {

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
    public static RecordHandler createFactory(Pair<String, DataHandler<?>> ... variables) {
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

    public Record fromObject(Object ... values) {
        if (values.length != handlers.length) {
            throw new IllegalArgumentException("Wrong data size! (Expected " + handlers.length + " is " + values.length);
        }
        Object[] data = new Object[values.length];
        for (int i=0 ; i<values.length ; i++) {
            data[i] = handlers[i].fromObject(values[i]);
        }
        return build(data);
    }

    public Record fromDouble(double ... values) {
        return fromDouble(values,0,values.length);
    }

    public Record fromDouble(double[] values, int from, int to) {
        if ((to-from) != handlers.length) {
            throw new IllegalArgumentException("Wrong data size! (Expected " + handlers.length + " is " + (to-from));
        }
        Object[] data = new Object[values.length];
        for (int i=0 ; i<handlers.length ; i++) {
            data[i] = handlers[i].fromDouble(values[i+from]);
        }
        return build(data);
    }


    private Record build(Object[] values) {
        if (values.length != handlers.length) {
            throw new IllegalArgumentException();
        }
        return new Record(i -> handlers[i], values);
    }

    public Record fromString(String... values) {
        return fromString(values,0,values.length);
    }

    public Record fromString(String[] values, int from, int to) {
        if ((to-from) != handlers.length) {
            throw new IllegalArgumentException("Wrong data size! (Expected " + handlers.length + " is " + (to-from) + ")");
        }
        Object[] data = new Object[handlers.length];
        for (int i=0 ; i<handlers.length ; i++) {
            data[i] = handlers[i].fromString(values[i+from]);
        }
        return build(data);
    }

    public Record fromObject(Map<String, Object> values) throws IllegalValueException {
        checkNumberOfVariables(values.size());
        Object[] data = new Object[handlers.length];
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String v = entry.getKey();
            Object o = entry.getValue();
            int variableIndex = getVariableIndex(v);
            if (variableIndex < 0) {
                throwUnknownVariableException(v);
            }
            DataHandler<?> handler = handlers[variableIndex];
//            if (!handler.checkType(o)) {
//                throwVariableTypeException(v, handler.getTypeOf().getTypeName(), o.getClass().getTypeName());
//            }
            data[variableIndex] = handler.fromObject(o);
        }
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

    public Record fromString(Map<String, String> values) throws IllegalValueException {
        checkNumberOfVariables(values.size());
        Object[] data = new Object[handlers.length];
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String v = entry.getKey();
            String o = entry.getValue();
            int variableIndex = getVariableIndex(v);
            if (variableIndex < 0) {
                throwUnknownVariableException(v);
            }
            DataHandler<?> handler = handlers[variableIndex];
            data[variableIndex] = handler.fromString(o);
        }
        return build(data);
    }

    public Record fromDouble(Map<String, Double> values) throws IllegalValueException {
        checkNumberOfVariables(values.size());
        Object[] data = new Object[handlers.length];
        for (Map.Entry<String, Double> entry : values.entrySet()) {
            String v = entry.getKey();
            Double d = entry.getValue();
            int variableIndex = getVariableIndex(v);
            if (variableIndex < 0) {
                throwUnknownVariableException(v);
            }
            DataHandler<?> handler = handlers[variableIndex];
            data[variableIndex] = handler.fromDouble(d);
        }
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
        /* TODO: Fix this method! */
        return true; //handlers[variableIndex].checkTypeCode(type);
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
        //TODO: Fix this method! The method should be removed.
        return "";//handlers[variableIndex].getTypeCode();
    }

    public String[] getVariables() {
        return variables.keySet().toArray(new String[variables.size()]);
    }

    public boolean checkValueFromString(String v, String value) {
        int variableIndex = getVariableIndex(v);
        if (variableIndex < 0) {
            return false;
        }
        return handlers[variableIndex].checkStringValue(value);
    }

    public boolean checkValuesFromStrings(String[] values) {
        return checkValuesFromStrings(values, 0, values.length);
    }

    public boolean checkValuesFromStrings(String[] values, int from, int to) {
        if ((to-from) != handlers.length) {
            return false;
        }
        return IntStream.range(0,handlers.length).allMatch(i -> handlers[i].checkStringValue(values[i+from]));
    }

    public static Signal<Record> buildTemporalSignal(RecordHandler handler, double[] time,
                                                     String[][] signal) throws IllegalValueException {
        Signal<Record> toReturn = new Signal<>();
        for (int i = 0; i < time.length; i++) {
            toReturn.add(time[i], handler.fromString(signal[i]));
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
            double[][][] signal) {
    	return new SpatioTemporalSignal<>(size, i -> buildTemporalSignal(handler, time, signal[i]));
    }

    @Override
    public Class<Record> getTypeOf() {
        return Record.class;
    }

    @Override
    public Record fromObject(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (o instanceof Record) {
            Record r = (Record) o;
            Object[] values = r.getValues();
            if (this.checkValues(values)) {
                return build(values);
            }
            throw new IllegalValueException("Illegal data type!");
        }
        throw new IllegalValueException("A record is espected. Here we have "+o.getClass().getName());
    }

    private boolean checkValues(Object[] values) {
        if (handlers.length == values.length) {
            for( int i=0 ; i<values.length ; i++ ) {
                if (!handlers[i].checkObjectValue(values[i])) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public Record fromString(String str) {
        String[] data = str.split(";");
        return fromString(data);
    }

    @Override
    public Record fromDouble(double value) {
        throw new IllegalValueException("A record cannot be built from a double!");
    }

    @Override
    public String toString(Record record) {
        return record.toString();
    }

    @Override
    public double toDouble(Record record) {
        throw new IllegalArgumentException();
    }

    @Override
    public boolean checkObjectValue(Object o) {
        if (o instanceof Record) {
            Record r = (Record) o;
            Object[] values = r.getValues();
            return (this.checkValues(values));
            }
        return false;
    }

    @Override
    public boolean checkStringValue(String value) {
        return checkValuesFromStrings(value.split(";"));
    }

    @Override
    public boolean checkDoubleValue(double value) {
        return false;
    }
}
