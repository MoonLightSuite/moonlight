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
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.quanticol.moonlight.signal;

import java.util.HashMap;

public class EnumerationHandler<T> implements DataHandler<T> {

    private final T[] values;
    private final Class<T> type;
    private final HashMap<String,T> stringMap;
    private final HashMap<T,Integer> indexOf;

    public EnumerationHandler(Class<T> type, T[] values) {
        this.values = values;
        this.type = type;
        this.stringMap = new HashMap<>();
        this.indexOf = new HashMap<>();
        initStringMap();
        initIndexMap();
    }

    private void initIndexMap() {
        for (int i=0 ; i<values.length ; i++ ) {
            indexOf.put(values[i],i);
        }
    }

    private void initStringMap() {
        for (T t: values ) {
            stringMap.put(t.toString(),t);
        }
    }

    @Override
    public Class<T> getTypeOf() {
        return type;
    }

    @Override
    public T fromObject(Object o) {
        if (type.isInstance(o)) {
            return type.cast(o);
        }
        throw  new IllegalValueException("Expected "+type.getName()+" was "+o);
    }

    @Override
    public T fromString(String str) {
        T value = stringMap.get(str);
        if (value != null) {
            return value;
        }
        throw new IllegalValueException("Unknown value "+str);
    }

    @Override
    public T fromDouble(double value) {
        int intValue = (int) value;
        if ((intValue<0)||(intValue>=values.length)) {
            throw new IllegalValueException("Illegal index "+value);
        }
        return values[intValue];
    }

    @Override
    public String stringOf(T t) {
        return t.toString();
    }

    @Override
    public double doubleOf(Object t) {
        if (type.isInstance(t)) {
            return indexOf(type.cast(t));
        }
        return Double.NaN;
    }

    public int indexOf(T t) {
        return indexOf.getOrDefault(t, -1);
    }
    @Override
    public boolean checkObjectValue(Object o) {
        return (type.isInstance(o)&&indexOf.containsKey(o));
    }

    @Override
    public boolean checkStringValue(String value) {
        return stringMap.containsKey(value);
    }

    @Override
    public boolean checkDoubleValue(double value) {
        int intValue = (int) value;
        return ((intValue<0)||(intValue>=values.length));
    }
}
