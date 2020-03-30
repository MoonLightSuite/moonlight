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

/**
 * A <code>DataHandler</code> is used to manage input/output of a signal value of type <code>S</code>.
 *
 * @author loreti
 *
 */
public interface DataHandler<S> {

    /**
     * Returns the handled data type.
     *
     * @return the handled data type.
     */
    Class<S> getTypeOf();

    /**
     * Cast an object <code>o</code> in the handled data type <code>S</code>.
     * @param o object to cast
     * @return result cast of <code>o</code>
     * @throws IllegalValueException if <code>o</code> is not a valid object.
     */
    S fromObject(Object o);

    /**
     * Parse a data item from a String.
     *
     * @param str string representation of the data.
     *
     * @return the converted data.
     * @throws IllegalValueException if the value is not a valid value.
     */
    S fromString(String str);

    /**
     * Convert a double value into the data item.
     *
     * @param value a double value.
     * @return the converted data item.
     * @throws IllegalValueException if the value is not a valid value.
     */
    S fromDouble(double value);

    /**
     * Return a string representation of a data item <code>s</code>.
     *
     * @param s data item to represent.
     * @return string representation of <code>s</code>.
     */
    String stringOf(S s);

    /**
     * Return a double representation of a data item <code>s</code>.
     *
     * @param s data item to represent.
     * @return double representation of <code>s</code>.
     */
    double doubleOf(S s);

    /**
     * Check if the object <code>o</code> is a valid data type.
     *
     * @param o a value.
     * @return true if <code>o</code> is a valid value for this handler.
     */
    boolean checkObjectValue(Object o);

    /**
     * Check if  <code>value</code> is a valid string representation for
     * handled data type.
     *
     * @param value a string representation.
     * @return true if <code>value</code> is a valid string for the handled data type.
     */
    boolean checkStringValue(String value);

    /**
     * Check if <code>value</code> is a valid double representation for
     * handled data type.
     *
     * @param value an double representation of data type.
     * @return true if <code>value</code> is a valid value.
     */
    default boolean checkDoubleValue(double value) {
        return true;
    }

    /**
     * A data handler for doubles.
     */
    DataHandler<Double> REAL = new DataHandler<Double>() {

        @Override
        public Class<Double> getTypeOf() {
            return Double.class;
        }

        /**
         * If  <code>value</code> is a Number, the doubleValue is returned.
         * If <code>value</code> is null, 0.0 is returned. Otherwise a {@link IllegalValueException} is thrown.
         *
         * @param value data to convert
         * @return a Double value
         * @throws IllegalValueException if <code>value</code> is not a double.
         */
        @Override
        public Double fromObject(Object value) {
            if (value == null) {
                return 0.0;
            }
            if (value instanceof Double) {
                return (Double) value;
            }
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            throw new IllegalValueException("Expected a double is "+value.toString());
        }

        @Override
        public Double fromString(String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                throw  new IllegalValueException(e);
            }
        }

        @Override
        public Double fromDouble(double value) {
            return value;
        }

        @Override
        public String stringOf(Double aDouble) {
            return aDouble.toString();
        }

        @Override
        public double doubleOf(Double aDouble) {
            return aDouble;
        }

        @Override
        public boolean checkObjectValue(Object o) {
            return (o instanceof Double);
        }

        @Override
        public boolean checkStringValue(String value) {
            try {
                Double.parseDouble(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

    };

    /**
     * A data handler for integers.
     */
    DataHandler<Integer> INTEGER = new DataHandler<Integer>() {

        @Override
        public Class<Integer> getTypeOf() {
            return Integer.class;
        }

        /**
         * If  <code>value</code> is a Number, the intValue is returned.
         * If <code>value</code> is null, 0.0 is returned. Otherwise a {@link IllegalValueException} is thrown.
         *
         * @param value data to convert
         * @return a Double value
         * @throws IllegalValueException if <code>value</code> is not a double.
         */
        @Override
        public Integer fromObject(Object value) {
            if (value == null) {
                return 0;
            }
            if (value instanceof Integer) {
                return (Integer) value;
            }
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            throw new IllegalValueException("Expected an Integer is "+value);
        }

        @Override
        public Integer fromString(String str) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                throw  new IllegalValueException(e);
            }
        }

        /**
         * Returns the greatest integer values less or equal to <code>value</code>.
         *
         * @param value a double value.
         * @return the greatest integer values less or equal to <code>value</code>.
         */
        @Override
        public Integer fromDouble(double value) {
            return (int) Math.floor(value);
        }

        @Override
        public String stringOf(Integer integer) {
            return integer.toString();
        }

        @Override
        public double doubleOf(Integer integer) {
            return integer.doubleValue();
        }

        @Override
        public boolean checkObjectValue(Object o) {
            return (o instanceof Integer);
        }

        @Override
        public boolean checkStringValue(String value) {
            try {
                Integer.parseInt(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    };

    /**
     * A data handler for booleans.
     */
    DataHandler<Boolean> BOOLEAN = new DataHandler<Boolean>() {

        @Override
        public Class<Boolean> getTypeOf() {
            return Boolean.class;
        }

        /**
         * Cast parameter <code>value</code> to an Boolean. If it is not of the right
         * class a {@link IllegalValueException} is thrown. If <code>value</code> is null,
         * false is returned.
         *
         * @param value data to convert
         * @return a Double value
         * @throws IllegalValueException if <code>value</code> is not a double.
         */
        @Override
        public Boolean fromObject(Object value) throws IllegalValueException {
            if (value == null) {
                return false;
            }
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            throw new IllegalValueException("Expected Boolea is "+value);
        }

        @Override
        public Boolean fromString(String str) throws IllegalValueException {
            return Boolean.parseBoolean(str);
        }

        /**
         * Returns true if <code>value</code> is greater than 0.0, false otherwise.
         *
         * @param value a double value.
         * @return true if <code>value</code> is greater than 0.0, false otherwise.
         */
        @Override
        public Boolean fromDouble(double value) {
            return value > 0;
        }

        @Override
        public String stringOf(Boolean aBoolean) {
            return aBoolean.toString();
        }

        @Override
        public double doubleOf(Boolean aBoolean) {
            return (aBoolean?1.0:-1.0);
        }

        @Override
        public boolean checkObjectValue(Object o) {
            return (o instanceof Boolean);
        }

        @Override
        public boolean checkStringValue(String value) {
            return true;
        }


    };

}
